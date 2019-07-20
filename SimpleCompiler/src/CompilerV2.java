import com.sun.corba.se.impl.io.TypeMismatchException;

import java.util.*;


public class CompilerV2 {
    int count;
    int globalCount;
    boolean inGlobal;
    String output;

    public static final String PREFIX = "local[";
    public static final String GLOBALPRE = "global[";
    public static final String POSTFIX = "]";
    public static final String SEMICO = ";";

    public static final String[] RESERVED_WORDS = {
            "int", "char", "if", "else", "while", "for", "break", "continue", "return", "true", "false", "switch", "case", "default",
            "void", "static", "public", "private"
    };

    public static final String[] COND_OPS = {
            "&&", "||", "<=", ">=", "&", "|", "<", ">", "==", "!="
    };
    public final char EOF = '啊';
    int cursor;
    String input;
    List<String> tokenlist;

    Stack<Character> ops;
    Stack<String> exp;

    Map<String, String> varpos;

    public boolean isReserved(String word){
        for(int i = 0; i < RESERVED_WORDS.length; i++){
            if(RESERVED_WORDS[i].equals(word)){
                return true;
            }
        }
        return false;
    }

    public void reservedCheck() throws Exception{
        String buff = parseToken();
        cursor -= buff.length();
    }

    public CompilerV2(String input){
        this.input = input+EOF;
        this.cursor = 0;
        this.tokenlist = new ArrayList<>();
        this.count = 0;
        this.globalCount = 0;
        this.ops = new Stack<>();

        this.exp = new Stack<>();

        this.output = "";

        this.inGlobal = true;
        varpos = new HashMap<>();

    }


    public String compile() throws Exception{
        return null;
    }





    public String parseProgram() throws Exception{
        String result = "";
        String meta = parseMetaStatement() + "\n";
        output += meta;
        String vars = parseVariableDeclaration() + "\n";
        output += vars;
        String funcs = parseFunctionDeclaration();
        output += funcs;

        output += "";
        return result;
    }

    public String parseVariableDeclaration() throws Exception{
        consumeSpace();
        boolean flag = true;
        String result = "";
        while(flag){
            int cur = cursor;
            int g = globalCount;
            int c = count;
            try {
                String type = parseType() + " ";
                result += type;
                consumeSpace();
                String tok = parseToken();
                result += tok;
                if(varpos.containsKey(tok)){
                    throw new Exception("In parseVariableDeclaration, already defined variable.");
                }
                if(inGlobal){
                    varpos.put(tok,GLOBALPRE + globalCount + POSTFIX);
                    globalCount ++;
                }else{
                    varpos.put(tok, PREFIX + count + POSTFIX);
                    count++;
                }
                consumeSpace();
                if(peek() == '='){
                    result += " "+match('=') + " ";

                    consumeSpace();;
                    result += parseExpression();
                }

                if(peek() == ';'){
                    result += match(';') + "\n";
                }else{
                    cursor = cur;
                    globalCount = g;
                    count = c;
                    flag = false;
                    break;
                }
            }catch(TypeMismatchException e){
                flag = false;
            }
        }
//        System.out.println(result);

        if(inGlobal){

            result =  "int " + GLOBALPRE + (globalCount) + POSTFIX+";";
        }else{

            result = "int " + PREFIX + (count) + POSTFIX+";";
        }


        return result;

    }

    public String parseOneVarDeclare() throws Exception{
        String result = "";
        result += parseType() + " ";

        consumeSpace();

        result += parseToken();

        consumeSpace();
        if(peek() == '='){
            String mid = " "+match('=') + " ";

            consumeSpace();;
            result = parseExpression() + mid + result;
        }


        result += match(';');

        return result;
    }



    public String parseType() throws TypeMismatchException,Exception {
        consumeSpace();
        String result = "";
        String here = input.substring(cursor);
        if(here.startsWith("int") || here.startsWith("char")){

            if(here.startsWith("char")){
                result += "char";
                cursor += 4;
            }else{
                result += "int";
                cursor += 3;
            }

            consumeSpace();

            if(peek() == '*'){
                while(peek() == '*'){
                    result += match('*');
                }
            }else if(peek() == '['){
                result += parseDeclareArrayAppendix();
            }



            return result;




        }else if(here.startsWith("void")){
            result += "void";
            cursor += 4;
            if(peek() == ' ' || peek() == '\n' || peek() == '\t'){
                return result;
            }

            throw new TypeMismatchException("UnsupportedTypeException.");
        }else{
//            System.out.println("here2:\n" + input.substring(0,cursor) + "\nend here 2");
            throw new TypeMismatchException("UnsupportedTypeException.");
        }
    }

    public String parseDeclareArrayAppendix() throws Exception{
        String result = "";
        while(peek() == '[' || peek() == ']'){
            result += match('[');
            result += match(']');
        }
        return result;
    }

    public String parseParameter() throws Exception{
        consumeSpace();
        String res = "";
        res += parseType() + " ";
        consumeSpace();
        res += parseToken();
        return res;
    }

    public String parseParamList() throws Exception{
        String res = "";
        consumeSpace();
        res += parseParameter();
        consumeSpace();
        res += parseParamListTail();
        return res;
    }

    public String parseParamListTail() throws Exception{
        String res = "";
        consumeSpace();
        if(peek() == ',') {
            res += match(',') + " ";
            consumeSpace();
            res += parseParameter();
            consumeSpace();
            res += parseParamListTail();
        }
        return res;
    }

    public String parseStatement() throws Exception,TypeMismatchException{

        String result = "";
        String here = input.substring(cursor);
        if(here.startsWith("if")){
            result += parseIfStatement();
        }else if(here.startsWith("while")){
            result += parseWhileStatement();
        }else if(here.startsWith("break")){
            result += parseBreakStatement();
        }else if(here.startsWith("continue")){
            result += parseContinueStatement();
        }else if(here.startsWith("return")){
            result += parseReturnStatement();
        }else{

            int now = cursor;
            String typewrong = "UnsupportedTypeException.";
            String usereserved = "Cannot use a reserved word as variable name.";
            try{

                parseType();
                System.out.println("here");
                cursor = now;
                result += parseAssignment();
            }catch(TypeMismatchException e){

                if(e.getMessage().equals(typewrong)){
                    cursor = now;
                    result += varpos.get(parseToken());
                    consumeSpace();
                    if(peek() == '('){
                        cursor = now;
                        result += parseGeneralFunctionCall();

                    }else{
                        cursor = now;
                        System.out.println("assignment  asd");
                        result += parseAssignment();
                    }

                }
            }

        }

        consumeSpace();
        if(peek() == '}'){
            cursor -= 2;
        }
        System.out.println("peek = " + peek());
        result += match(';');

        return result;
    }

    public String parseStatementList() throws Exception{
        String res = "\t";
        consumeSpace();
        res += parseVariableDeclaration() + "\n        ";
        consumeSpace();

        while(peek() != '}'){

            res += parseStatement() + "\n\t";
//            System.out.println("parse statement list");
            consumeSpace();
        }


        consumeSpace();

//        res += parseStatementListTail();

        return res;
    }

    public String parseStatementListTail() throws Exception{
        String res = "";
        consumeSpace();
        while(peek() != '}'){

            res += parseStatement() + "\n\t";
            consumeSpace();
            res += parseStatementListTail();
            consumeSpace();
        }

        return res;
    }

    public String parseBlockStatements() throws Exception{
        consumeSpace();
        String res = "";
        res += match('{') + "\n\t";
        consumeSpace();
        res += parseStatementList();
        consumeSpace();
//        System.out.println("block statements");
        res += "\n"+match('}')+"\n";
        return res;
    }

    public String parseAssignment() throws Exception{
        consumeSpace();
        String result = "";

        if(isLegal(peek())){
            result += varpos.get(parseToken()) + " ";
            consumeSpace();
            if(peek() == '=') {
                result += match('=') + " ";
                consumeSpace();
                result += parseExpression();
                consumeSpace();

            }else{
                String here = input.substring(cursor);
                if(here.startsWith("*=") || here.startsWith("+=") || here.startsWith("-=") || here.startsWith("/=") || here.startsWith("%=") ){
                    result += " " + freeMatch();
                    result += freeMatch() + " ";
                    consumeSpace();
                    result += parseExpression();
                }else if(here.startsWith("++") || here.startsWith("--")){
                    result += " " + freeMatch() + freeMatch() + " ";
                    consumeSpace();
                }
            }
        }else{
            if(peek() == '+'){
                result += match('+') + match('+') + parseToken();
            }else if(peek() == '-'){
                result += match('-') + match('-') + parseToken();
            }else{
                throw new Exception("Wrong Assignment.");
            }
        }
//        result += match(';');
//        System.out.println(result);
        return result;
    }

    public String parseGeneralFunctionCall() throws Exception{
//        System.out.println("general function call");
        String res = "";
        consumeSpace();
        res += parseToken();
        consumeSpace();
        res += match('(');
        consumeSpace();
        res += parseExpressionList();
        consumeSpace();
        res += match(')');
        consumeSpace();

        res += match(';');
//        System.out.println(res);
        consumeSpace();

        return res;
    }

    public String parseWhileStatement() throws Exception{
        consumeSpace();
        String here = input.substring(cursor);
        String res = "";
        if(here.startsWith("while")){
            res += parseReservedWord();
            consumeSpace();
            res += match('(');
            consumeSpace();
            res += parseExpression();
            consumeSpace();
            res += match(')');
            res += parseBlockStatements();
            return res;
        }else{
            throw new Exception("illegal word:");
        }

    }

    public String parseForStatement() throws Exception{
        String here = input.substring(cursor);
        String res = "";
        if(here.startsWith("for")){
            consumeSpace();
            res += match('(');
            consumeSpace();
            res += parseAssignment();
            consumeSpace();
            res += parseConditionalExpression();
            consumeSpace();
            res += parseAssignment();
            consumeSpace();
            res += parseBlockStatements();
            return res;
        }else{
            throw new Exception("illegal word.");
        }

    }

    public String parseIfStatement() throws Exception{
        String result = "";
        String here = input.substring(cursor);
        consumeSpace();
        if(here.startsWith("if")){

            result += parseReservedWord();

            consumeSpace();
            result += match('(');
            consumeSpace();
            result += parseExpression();
            consumeSpace();
            result += match(')');
            consumeSpace();
            result += parseBlockStatements();
            consumeSpace();
            here = input.substring(cursor);
            if(here.startsWith("else")){
                result += parseReservedWord();
                consumeSpace();
                result += parseBlockStatements();
                consumeSpace();
            }
        }else{
            throw new Exception("fail to parse If.");
        }

        return result;
    }

    public String parseReturnStatement() throws Exception{
        consumeSpace();
        String result = "";
        String here = input.substring(cursor);
        if(here.startsWith("return")){
            result += parseReservedWord();
            consumeSpace();
            if(peek() != ';'){
                result += parseExpression();
            }
            result += match(';');
            consumeSpace();
            return result;
        }else{
            throw new Exception("Wrong Return Call.");
        }
    }


    public String parseBreakStatement() throws Exception{
        consumeSpace();
        String result = "";
        String here = input.substring(cursor);
        if(here.startsWith("break")){
            result += parseReservedWord();
            consumeSpace();
            result += match(';');
            consumeSpace();
            return result;
        }else{
            throw new Exception("Wrong Break Call.");
        }
    }

    public String parseContinueStatement() throws Exception{
        consumeSpace();
        String result = "";
        String here = input.substring(cursor);
        if(here.startsWith("continue")){
            result += parseReservedWord();
            consumeSpace();
            result += match(';');
            consumeSpace();
            return result;
        }else{
            throw new Exception("Wrong Continue Call.");
        }
    }


    public String parseFunctionDeclaration() throws Exception{
        String res = "";
        inGlobal = false;
        consumeSpace();
        while(cursor < input.length() - 1){
            count = 0;
            String returnType = parseType();
            consumeSpace();
            String funcName = parseToken();
            consumeSpace();
            String left = match('(')+"";
            consumeSpace();
            String paramlist = "";
            if(peek() != ')'){
                paramlist += parseParamList();
                consumeSpace();
            }

            String right = match(')') + "";
            consumeSpace();
            String block = parseBlockStatements();
            res += returnType + " " + funcName + left + paramlist + right + "\n" + block;
        }
        return res;
    }

    public String parseFunctionCall() throws Exception{
        return null;
    }



    public String parseMetaStatement() throws Exception{
        consumeSpace();

        String result = "";
        char c = peek();
        if(c == '#'){
            result += freeMatch();
            while(peek() != '\n' && peek() != EOF){
                result += freeMatch();
            }
            return result;
        }else if(c == '"'){
            result += freeMatch();
            while(peek() != '"'  && peek() != EOF){
                result += freeMatch();
            }
            return result;
        }else if(c == '/'){
            result += match('/');
            char c1 = peek();
            if(c1 == '/'){
                result += freeMatch();
                while(peek() != '\n' && peek() != EOF){
                    result += freeMatch();
                }
                return result;
            }else if(c1 == '*'){
                result += match('*');

                while(peek() != '\n' && peek() != EOF){
                    result += freeMatch();
                }

                if(!result.substring(result.length()-2).equals("*/")){
                    throw new Exception("MetaStatementFormatException");
                }
            }
            return result;
        }

        else{
            return "";
        }
    }

    public String parseExpression() throws Exception{
        String res = "";
        consumeSpace();
        if(peek() != '"'){

            String head = parseTerm();

            consumeSpace();

            String tail = parseExpressionTail();
            System.out.println(exp.peek());
            if(tail.equals("")){
                return head;
            }
            return exp.peek();
        }else{
            res += parseString();
        }

        return res;
    }

    public String parseExpressionTail() throws Exception{
        char c = peek();
        String result = "";
        if( c == '+' || c == '-'){


            result += c;
            ops.push(c);
            cursor ++;
            consumeSpace();

            String term = parseTerm();
            String right = exp.pop();
            String left = exp.pop();

            String notation = PREFIX + count++ + POSTFIX;
            exp.push(notation);
            char tok = ops.pop();
            //System.out.println(notation + " = " + left + " " + tok + " " + right +";");
//            output += notation + " = " + left + " " + tok + " " + right +";\n";
            String tail = parseExpressionTail();
//            exp.push(term);
//            exp.push(tail);


            return notation;

        }else{
            return "";
        }
    }

    public String parseConditionalExpression() throws Exception{
        String res = "";
        consumeSpace();
        if(peek() == '!'){
            res += match('!');
            res += parseConditionalExpression();
        }else{
            String s1 = parseExpression();
            String op = parseConditionOp();
            String s2 = parseExpression();
            res += s1 + op + s2;
        }
        return res;
    }

    public String parseConditionOp(){
        String res = "";
        consumeSpace();
        String here = input.substring(cursor);
        for(int i = 0; i < COND_OPS.length; i++){
            String op = COND_OPS[i];
            if(here.startsWith(op)){
                res += op;
                cursor += op.length();
                break;
            }
        }
        return res;
    }

    public String parseConditionalExpressionTail() throws Exception{
        String res = "";
        consumeSpace();

        String here = input.substring(cursor);
        if(here.startsWith("&&")){
            res += " &&";
            cursor += 2;
        }else if(here.startsWith("&")){
            res += " &";
            cursor += 1;
        }else if(here.startsWith("||")){
            res += " ||";
            cursor += 2;
        }else if(here.startsWith(" |")){
            cursor += 1;
        }
        consumeSpace();
        res += parseFactor();
        consumeSpace();
        res += parseConditionalExpressionTail();

        return res;
    }



    public String parseExpressionList() throws Exception{
        String res = "";
        consumeSpace();
        if(peek() == ')'){
            return "";
        }
        res += parseExpression();

        consumeSpace();
        res += parseExpressionListTail();


        return res;
    }

    public String parseExpressionListTail() throws Exception{
        String res = "";
        consumeSpace();
        if(peek() == ','){
            res += match(',') + " ";
            consumeSpace();
            res += parseExpression();
            consumeSpace();
            res += parseExpressionListTail();
        }
        return res;
    }

    public String parseTerm() throws Exception{

        String head = parseFactor();

        String tail = parseTermTail();

        if(tail.equals("")){

            return head;
        }
//        System.out.println(exp.peek());
        return exp.peek();
//        String output = "local[" + count++ +"]";
//        System.out.println(output + " = " + exp.pop() + " " + stack.pop() + " " + exp.pop()+";");
//        exp.push(output);

    }

    public String parseTermTail() throws Exception{
        char c = peek();
        String result = "";

        if(c == '*' || c == '/' || c == '%'){

            result += c;
            ops.push(c);
            cursor ++;
            String factor = parseFactor();

            if(cursor == input.length()-1){
                String right = exp.pop();
                String left = exp.peek();
                char tok = ops.pop();
                String equationleft = PREFIX + count++ + POSTFIX;
//                output += equationleft + " = " + left + " " + tok + " " + right +";\n";
                return equationleft;
            }
            String right = exp.pop();
            String left = exp.pop();

            String equationleft = PREFIX + count++ + POSTFIX;
            exp.push(equationleft);
            char tok = ops.pop();
            //System.out.println(equationleft + " = " + left + " " + tok + " " + right +";");
            output += equationleft + " = " + left + " " + tok + " " + right +";\n";
            String tail = parseTermTail();


            return equationleft;
        }else{
//            String res = "local["+count++ +"]";
//            System.out.println(res + " = " + exp.pop() + " " + stack.pop()+ " " + exp.pop()+";");
//            exp.push(res);
            return "";
        }
    }

    public String parseSingleCondition() throws Exception{
        consumeSpace();
        String res = "";
        res += parseFactor();
        consumeSpace();

        String here = input.substring(cursor);
        if(here.startsWith("&&")){
            res += " &&";
            cursor += 2;
            consumeSpace();
            res += parseFactor();
        }else if(here.startsWith("&")){
            res += " &";
            cursor += 1;
            consumeSpace();
            res += parseFactor();
        }else if(here.startsWith("||")){
            res += " ||";
            cursor += 2;
            consumeSpace();
            res += parseFactor();
        }else if(here.startsWith(" |")){
            res += " |";
            cursor += 1;
            consumeSpace();
            res += parseFactor();
        }
        consumeSpace();

        return res;
    }


    public String parseFactor() throws Exception {

        consumeSpace();
        char c = peek();

        if(isDigit(c)){
            String res = parseNumber();

//            return res;
            String notation = PREFIX+ count++ + POSTFIX;
            exp.push(notation);
            //System.out.println( notation + " = " + res+";");
//            output += notation + " = " + res+";\n";
            return notation;

        }else if(isLetter(c)){


            String res = parseToken();
            consumeSpace();

            if(peek() != '('){

                return res;
            }else{
                res += match('(');
                consumeSpace();
                if(peek() != ')'){
                    res += parseExpressionList();
                    consumeSpace();
                }
                res += match(')');
                return res;
            }


        }else if(c == '('){


            String result = "(";
            cursor++;
            result += parseExpression();
            c = peek();
            if(c == ')'){
                cursor++;
                return result + ")";
            }else{
                throw new Exception("FactorFormatException: a ) is anticipated.");
            }
        }
        else{
            throw new Exception("FactorFormatException: illegal starting pattern.");
        }
    }

    public String parseAddOP() throws Exception{
        char c = peek();
        if(c =='+' || c =='-'){
            cursor ++;
            return c+"";
        }else{
            throw new Exception("AdditionOperatorException");
        }
    }

    public String parseMultiOP() throws Exception{
        char c = peek();
        if(c == '*' || c == '/' || c == '%'){
            cursor ++;
            return c+"";
        }else{
            throw new Exception("MultiplicationOperatorException");
        }
    }


    public String parseToken() throws Exception{
        String result = "";
        if(isLetter(peek())){
            result += peek();
            cursor++;
        }else{
            System.out.println(input.substring(0,cursor));
            throw new Exception("TokenFormatException");
        }
        while(isLegal(peek()) && cursor < input.length()){
            result += peek();
            cursor ++;
        }
        if(isReserved(result)){
            throw new Exception("Cannot use a reserved word as variable name.");
        }

        return result;
    }

    public String parseNumber() throws Exception{
        String result = "";
        char c = peek();
        if(!isDigit(c)){
            throw new Exception("NumberFormatException");
        }
        while(cursor < input.length()){

            if(isDigit(peek())){
                result += peek();
                cursor ++;
            }else{
                break;
            }

        }
//        output+=result;
        return result;
    }


    public char peek(){

        return input.charAt(cursor);
    }

    public String parseReservedWord() throws Exception{
        String result = "";

        String here = input.substring(cursor);
        if(here.startsWith("int")){
            result += "int";
            cursor += 3;
        }else if(here.startsWith("char ")){
            result += "char";
            cursor += 4;
        }else if(here.startsWith("if(") || here.startsWith("if(")){
            result += "if";
            cursor += 2;
        }else if(here.startsWith("else")){
            cursor += 4;
            if(isLegal(peek())){
                cursor -= 4;
            }else{
                result += "else";
            }
        }else if(here.startsWith("for")){
            cursor += 3;
            if(isLegal(peek())){
                cursor -= 3;
            }else{
                result += "for";
            }
        }else if(here.startsWith("break;")){
            result += "break";
            cursor += 5;
        }else if(here.startsWith("continue;")){
            result += "continue";
            cursor += 8;
        }
        consumeSpace();

//        output+=result;
        return result;
    }

    public String parseString() throws Exception{
        char c = peek();
        String result = "";
        result += match('"');
        while(peek() != '"' && peek() != EOF){
            result += freeMatch();
        }
        result += match('"');
        output += result;
        return result;
    }

    public String match(char c) throws Exception{
        if(c == peek()){
            cursor ++;
            return c+"";
        }else{
//            System.out.println(output);
            System.out.println(input.substring(0,cursor));
            throw new Exception("MisMatchedException: expect "+c+", but see "+peek()+".");
        }
    }

    public String freeMatch(){
        String c = peek() + "";
        cursor ++ ;
        return c;
    }

    public void consumeSpace(){
        char c = peek();
        while(c == ' ' || c == '\n' || c == '\t'){
            freeMatch();
            c = peek();
        }
    }




    public static boolean isLegal(char c){
        return isDigit(c) || isLetter(c) || c == '_' || c == '$';
    }

    public static boolean isLetter(char c){
        int n = c;
        return (n >= 65 && n <= 90) || (n >= 97 && n <= 122);
    }

    public static boolean isDigit(char c){
        return c >= 48 && c <= 57;
    }





    public static void main(String[] args) {

        double[] local = new double[1000];


        String input = "#include <stdio.h>\n" +
                "\n" +
                "int globalname;\n" +
                "int globalage;\n" +
                "int globalfff;\n" +
                "\n" +
                "void foo(int a, int b){\n" +
                "\tint v1;\n" +
                "\tint v2;\n" +
                "\tint v3;\n" +
                "\tv3 = 5+6-7;\n" +
                "\tv2 = 1*(2+3);\n" +
                "\tprintf(\"%s%n\",v3);\n" +
                "}\n" +
                "\n" +
                "void boo(){\n" +
                "\tglobalname = 100;\n" +
                "}";

            String input2 = "2*(1+3)";
        CompilerV2 compiler = new CompilerV2(input);


        System.out.println(input);


        try {
            String str = compiler.parseProgram();
            String output = compiler.output;
            System.out.println("\n------------------------------------------\n"+output);




        }catch(Exception e){
            System.out.println("Throw expcetion: " + e.getMessage());
        }


    }
}
