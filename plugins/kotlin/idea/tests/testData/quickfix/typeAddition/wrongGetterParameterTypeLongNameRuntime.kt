// "Change getter type to HashSet<Int>" "true"

class A() {
    val i: java.util.HashSet<Int>
        get(): <caret>Any = java.util.LinkedHashSet<Int>()
}

// FUS_QUICKFIX_NAME: org.jetbrains.kotlin.idea.quickfix.ChangeAccessorTypeFixFactory$createAction$1
// FUS_K2_QUICKFIX_NAME: org.jetbrains.kotlin.idea.k2.codeinsight.fixes.ChangeAccessorTypeFixFactory$getFixes$1