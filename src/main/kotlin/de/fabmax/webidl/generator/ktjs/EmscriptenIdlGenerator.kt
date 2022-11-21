package de.fabmax.webidl.generator.ktjs

import de.fabmax.webidl.generator.CodeGenerator
import de.fabmax.webidl.model.*
import java.io.Writer

class EmscriptenIdlGenerator : CodeGenerator() {

    var outputIdlFileName = "out.idl"

    init {
        outputDirectory = "./generated/emscripten"
    }

    override fun generate(model: IdlModel) {
        createOutFileWriter(outputIdlFileName).use { w ->
            w.write("//\n")
            w.write("// Emscripten / Web IDL Binder compatible .idl file\n")
            w.write("// Generated by webidl-util from model ${model.name}\n")
            w.write("//\n\n")

            model.interfaces.sortedBy { it.name }
                .filter { !it.hasDecorator(IdlDecorator.NON_JS) }
                .forEach { it.writeTo(w) }

            model.enums.sortedBy { it.name }
                .filter { !it.hasDecorator(IdlDecorator.NON_JS) }
                .forEach { it.writeTo(w) }
        }
    }

    private fun IdlInterface.writeTo(w: Writer) {
        getDecoratorString()?.let { w.write("$it\n") }

        w.write("interface $name {\n")
        functions.filter { !it.hasDecorator(IdlDecorator.NON_JS) }.forEach { func ->
            w.write("    ${func.toIdl()}\n")
        }
        attributes.filter { !it.hasDecorator(IdlDecorator.NON_JS) }.forEach { attr ->
            w.write("    ${attr.toIdl()}\n")
        }
        w.write("};\n")
        superInterfaces.forEach { w.write("$name implements $it;\n") }
        w.write("\n")
    }

    private fun IdlEnum.writeTo(w: Writer) {
        if (decorators.isNotEmpty()) {
            w.write(decoratorsToStringOrEmpty())
            w.write("\n")
        }

        getDecoratorString()?.let { w.write("${it}\n") }
        w.write(decoratorsToStringOrEmpty())
        w.write("enum $name {\n")
        w.write(values.joinToString(",\n    ", "    ", "\n") { "\"$it\"" })
        w.write("};\n\n")
    }

    private fun IdlFunction.toIdl(): String {
        val str = StringBuilder()
        getDecoratorString()?.let { str.append("$it ") }
        if (isStatic) {
            str.append("static ")
        }
        str.append(returnType).append(" ")
        str.append(name)
        str.append("(").append(parameters.joinToString(", ") { it.toIdl() }).append(");")
        return str.toString()
    }

    private fun IdlFunctionParameter.toIdl(): String {
        val str = StringBuilder()
        getDecoratorString()?.let { str.append("$it ") }
        if (isOptional) {
            str.append("optional ")
        }
        str.append("$type $name")
        return str.toString()
    }

    private fun IdlAttribute.toIdl(): String {
        val str = StringBuilder()
        getDecoratorString()?.let { str.append("$it ") }
        if (isStatic) {
            str.append("static ")
        }
        if (isReadonly) {
            str.append("readonly ")
        }
        str.append("attribute ").append(type).append(" ").append(name).append(";")
        return str.toString()
    }

    private fun IdlDecoratedElement.getDecoratorString(): String? {
        val filteredDecos = decorators.filter { it.key in includedDecorators }
        if (filteredDecos.isEmpty()) {
            return null
        }

        return filteredDecos.joinToString(prefix = "[", postfix = "]") {
            if (it.value == null) {
                it.key
            } else {
                "${it.key}=\"${it.value}\""
            }
        }
    }

    companion object {
        private val includedDecorators = setOf(
            IdlDecorator.CONST,
            IdlDecorator.JS_IMPLEMENTATION,
            IdlDecorator.NO_DELETE,
            IdlDecorator.PREFIX,
            IdlDecorator.REF,
            IdlDecorator.VALUE
        )
    }
}