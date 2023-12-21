package net.ultragrav.knet.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*
import net.ultragrav.knet.ProxiedInterface

@OptIn(KspExperimental::class)
class KNetProcessor(val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private val visitor = ClassVisitor()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbolsWithAnnotation(ProxiedInterface::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .forEach { it.accept(visitor, Unit) }
        return emptyList()
    }

    inner class ClassVisitor : KSVisitorVoid() {
        private fun generateTypeArgumentString(typeArgument: KSTypeArgument): String {
            val type = typeArgument.type ?: return "*"
            val varianceStr = when (typeArgument.variance) {
                Variance.STAR -> throw IllegalStateException("Star projection cannot have type")
                Variance.INVARIANT -> ""
                Variance.CONTRAVARIANT -> "in "
                Variance.COVARIANT -> "out "
            }
            return varianceStr + generateTypeString(type.resolve())
        }

        private fun generateTypeString(type: KSType): String {
            // Check if the type has a KClass and get its qualified name
            val qualifiedName = type.declaration.qualifiedName?.asString() ?: "Unknown"

            return "$qualifiedName${
                if (type.arguments.isEmpty()) "" else "<${
                    type.arguments.joinToString(", ") {
                        generateTypeArgumentString(
                            it
                        )
                    }
                }>"
            }"
        }

        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            if (classDeclaration.classKind != ClassKind.INTERFACE) return
            if (!classDeclaration.isAnnotationPresent(ProxiedInterface::class)) return

            val toImplement = classDeclaration.getAllFunctions().filter { it.isAbstract }

            if (toImplement.any { !it.modifiers.contains(Modifier.SUSPEND) }) {
                environment.logger.error("All functions in a proxied interface must be suspend", classDeclaration)
                return
            }

            val proxyFile = environment.codeGenerator.createNewFile(
                dependencies = Dependencies(true, classDeclaration.containingFile!!),
                packageName = classDeclaration.packageName.asString(),
                fileName = "${classDeclaration.simpleName.asString()}Proxy"
            )

            val functions = StringBuilder()
            val callHandlerEntries = StringBuilder()
            toImplement.forEach { function ->
                val args =
                    function.parameters.joinToString(", ") { "${it.name!!.asString()}: ${generateTypeString(it.type.resolve())}" }
                val args2 = function.parameters.joinToString(", ") {
                    "KNet.serializer.serialize<${generateTypeString(it.type.resolve())}>(${it.name!!.asString()})"
                }
                val returnType = generateTypeString(function.returnType!!.resolve())
                functions.appendLine(
                    """
                    override suspend fun ${function.simpleName.asString()}($args): $returnType {
                        return KNet.serializer.deserialize<$returnType>(
                            provider.callProxyFunction("${classDeclaration.qualifiedName!!.asString()}", "${function.simpleName.asString()}", arrayOf($args2))
                        )
                    }
                """.trimIndent()
                )

                callHandlerEntries.append(
                    "\"${function.simpleName.asString()}\" -> KNet.serializer.serialize<$returnType>(proxy.${function.simpleName.asString()}(${
                        function.parameters.mapIndexed { index, param ->
                            "KNet.serializer.deserialize<${generateTypeString(param.type.resolve())}>(args[$index])"
                        }.joinToString(", ")
                    }))\n"
                )
            }

            val classStr = """
                |package ${classDeclaration.packageName.asString()}     
                |
                |import net.ultragrav.knet.serialization.serialize
                |import net.ultragrav.knet.serialization.deserialize
                |import net.ultragrav.knet.KNet
                |
                |class ${classDeclaration.simpleName.asString()}Proxy(val provider: net.ultragrav.knet.ProxyCallProvider) : ${classDeclaration.qualifiedName!!.asString()} {
                |${functions.toString().prependIndent("    ")}
                |}
            """.trimMargin()

            proxyFile.write(classStr.toByteArray())
            proxyFile.close()

            val callHandlerFile = environment.codeGenerator.createNewFile(
                dependencies = Dependencies(true, classDeclaration.containingFile!!),
                packageName = classDeclaration.packageName.asString(),
                fileName = "${classDeclaration.simpleName.asString()}CallHandler"
            )

            val callHandlerStr = """
                |package ${classDeclaration.packageName.asString()}
                |
                |import net.ultragrav.knet.serialization.serialize
                |import net.ultragrav.knet.serialization.deserialize
                |import net.ultragrav.knet.KNet
                |
                |class ${classDeclaration.simpleName.asString()}CallHandler(val proxy: ${classDeclaration.qualifiedName!!.asString()}) 
                |    : net.ultragrav.knet.ProxyCallHandler<${classDeclaration.qualifiedName!!.asString()}> {
                |    override suspend fun callProxyFunction(functionName: String, args: Array<ByteArray>): ByteArray {
                |        return when (functionName) {
                |${callHandlerEntries.toString().prependIndent("            ")}
                |            else -> throw IllegalArgumentException("Invalid function: ${"\$functionName"}")
                |        }
                |    }
                |}
            """.trimMargin()

            callHandlerFile.write(callHandlerStr.toByteArray())
            callHandlerFile.close()
        }
    }
}