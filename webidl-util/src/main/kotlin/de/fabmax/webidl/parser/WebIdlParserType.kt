package de.fabmax.webidl.parser

import de.fabmax.webidl.model.IdlType

enum class WebIdlParserType {
    Root {
        override fun possibleChildren() = listOf(Interface, Enum, LineComment, BlockComment, Decorators, Implements, Dictionary, Includes, TypeDef, Namespace)
        override suspend fun matches(stream: WebIdlStream) = false
        override fun newParser(parserState: WebIdlParser.ParserState) = RootParser(parserState)
    },

    Dictionary {
        override fun possibleChildren() = listOf(Decorators, LineComment, BlockComment, Member)
        override suspend fun matches(stream: WebIdlStream) = stream.startsWith("dictionary")
        override fun newParser(parserState: WebIdlParser.ParserState) = parserState.pushParser(
            DictionaryParser(
                parserState
            )
        )
    },

    Member {
        override fun possibleChildren(): List<WebIdlParserType> = emptyList()
        override suspend fun matches(stream: WebIdlStream): Boolean {
            var line = stream.pollUntilPattern(";")?.first ?: return false
            if (line.startsWith("required")) {
                line = line.substring("required".length + 1)
            }
            return IdlType.startsWithType(line)
        }

        override fun newParser(parserState: WebIdlParser.ParserState) = parserState.pushParser(
            MemberParser(
                parserState
            )
        )
    },

    Interface {
        override fun possibleChildren() = listOf(Decorators, LineComment, BlockComment, Attribute, Constructor, Function, SetLike)
        override suspend fun matches(stream: WebIdlStream) = stream.startsWith("interface") || stream.startsWith("partial interface")
        override fun newParser(parserState: WebIdlParser.ParserState) = parserState.pushParser(
            InterfaceParser(
                parserState
            )
        )
    },

    SetLike {
        override fun possibleChildren(): List<WebIdlParserType> = emptyList()
        override suspend fun matches(stream: WebIdlStream) = stream.startsWith("readonly setlike")
        override fun newParser(parserState: WebIdlParser.ParserState) = parserState.pushParser(
            SetLikeParser(
                parserState
            )
        )
    },

    Attribute {
        override fun possibleChildren(): List<WebIdlParserType> = emptyList()
        override suspend fun matches(stream: WebIdlStream) = stream.startsWith("attribute")
                || stream.startsWith("readonly attribute")
                || stream.startsWith("static attribute")
                || stream.startsWith("static readonly attribute")
        override fun newParser(parserState: WebIdlParser.ParserState) = parserState.pushParser(
            AttributeParser(
                parserState
            )
        )
    },

    Function {
        override fun possibleChildren(): List<WebIdlParserType> = listOf(Decorators, FunctionParameter)
        override suspend fun matches(stream: WebIdlStream): Boolean {
            var line = stream.pollUntilPattern("[\\(]", "[,;\\{\\}]")?.first ?: return false
            if (line.startsWith("static")) {
                line = line.substring(7)
            }
            return IdlType.startsWithType(line)
        }
        override fun newParser(parserState: WebIdlParser.ParserState) = parserState.pushParser(
            FunctionParser(
                parserState
            )
        )
    },

    Constructor {
        override fun possibleChildren(): List<WebIdlParserType> = listOf(Decorators, FunctionParameter)
        override suspend fun matches(stream: WebIdlStream): Boolean  = stream.startsWith("constructor")
        override fun newParser(parserState: WebIdlParser.ParserState) = parserState.pushParser(
            ConstructorParser(
                parserState
            )
        )
    },

    FunctionParameter {
        override fun possibleChildren(): List<WebIdlParserType> = emptyList()
        override suspend fun matches(stream: WebIdlStream): Boolean {
            val line = stream.pollUntilPattern("[,\\)]", "[;\\{\\}]")?.first ?: return false
            return line.startsWith("optional") || IdlType.startsWithType(line)
        }
        override fun newParser(parserState: WebIdlParser.ParserState) = parserState.pushParser(
            FunctionParameterParser(
                parserState
            )
        )
    },

    Enum {
        override fun possibleChildren(): List<WebIdlParserType> = emptyList()
        override suspend fun matches(stream: WebIdlStream) = stream.startsWith("enum")
        override fun newParser(parserState: WebIdlParser.ParserState) = parserState.pushParser(EnumParser(parserState))
    },

    LineComment {
        override fun possibleChildren(): List<WebIdlParserType> = emptyList()
        override suspend fun matches(stream: WebIdlStream) = stream.startsWith("//")
        override fun newParser(parserState: WebIdlParser.ParserState) = parserState.pushParser(
            LineCommentParser(
                parserState
            )
        )
    },

    BlockComment {
        override fun possibleChildren(): List<WebIdlParserType> = emptyList()
        override suspend fun matches(stream: WebIdlStream) = stream.startsWith("/*")
        override fun newParser(parserState: WebIdlParser.ParserState) = parserState.pushParser(
            BlockCommentParser(
                parserState
            )
        )
    },

    Decorators {
        override fun possibleChildren(): List<WebIdlParserType> = emptyList()
        override suspend fun matches(stream: WebIdlStream) = stream.startsWith("[")
        override fun newParser(parserState: WebIdlParser.ParserState) = parserState.pushParser(
            DecoratorParser(
                parserState
            )
        )
    },

    Implements {
        override fun possibleChildren(): List<WebIdlParserType> = emptyList()
        override suspend fun matches(stream: WebIdlStream) = stream.pollUntilPattern("\\simplements\\s", "[;,\\{\\}]") != null
        override fun newParser(parserState: WebIdlParser.ParserState) = parserState.pushParser(
            ImplementsParser(
                parserState
            )
        )
    },

    Includes {
        override fun possibleChildren(): List<WebIdlParserType> = emptyList()
        override suspend fun matches(stream: WebIdlStream) = stream.pollUntilPattern("\\sincludes\\s", "[;,\\{\\}]") != null
        override fun newParser(parserState: WebIdlParser.ParserState) = parserState.pushParser(
            IncludesParser(
                parserState
            )
        )
    },

    TypeDef {
        override fun possibleChildren(): List<WebIdlParserType> = listOf(Decorators)
        override suspend fun matches(stream: WebIdlStream) = stream.startsWith("typedef ")
        override fun newParser(parserState: WebIdlParser.ParserState) = parserState.pushParser(
            TypeDefParser(
                parserState
            )
        )
    },

    Namespace {
        override fun possibleChildren(): List<WebIdlParserType> = listOf(Constant)
        override suspend fun matches(stream: WebIdlStream) = stream.startsWith("namespace ")
        override fun newParser(parserState: WebIdlParser.ParserState) = parserState.pushParser(
            NamespaceParser(
                parserState
            )
        )
    },

    Constant {
        override fun possibleChildren(): List<WebIdlParserType> = listOf()
        override suspend fun matches(stream: WebIdlStream) = stream.startsWith("const ")
        override fun newParser(parserState: WebIdlParser.ParserState) = parserState.pushParser(
            ConstantParser(
                parserState
            )
        )
    },;

    abstract fun possibleChildren(): List<WebIdlParserType>
    abstract suspend fun matches(stream: WebIdlStream): Boolean
    abstract fun newParser(parserState: WebIdlParser.ParserState): ElementParser
}