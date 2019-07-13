import java.util.ArrayList;
import java.util.List;

public class MyCompiler {

    public final char EOF = '啊';
    int cursor;
    String input;
    List<String> tokenlist;

    public MyCompiler(String input){
        this.input = input+EOF;
        this.cursor = 0;
        this.tokenlist = new ArrayList<>();
    }


    public String compile() throws Exception{
        return null;
    }





    public String parseProgram() throws Exception{
        String result = "";
        String meta = parseMetaStatement();
        String vars = parseVariableDeclaration();

        result += meta + vars;
        return result;
    }

    public String parseVariableDeclaration() throws Exception{
        return null;
    }

    public String parseOneVarDeclare() throws Exception{
        String result = "";
        result += parseType() + " ";

        consumeSpace();

        result += parseToken();

        consumeSpace();
        if(peek() == '='){
            result += " "+match('=') + " ";

            consumeSpace();;
            result += parseExpression();
        }


            result += match(';');

        return result;
    }

    public String parseType() throws Exception{
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




        }else{
            throw new Exception("UnsupportedTypeException.");
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

    public String parseStatement() throws Exception{
        String result = "";
        String here = input.substring(cursor);
        if(here.startsWith("if(") || here.startsWith("if ")){

        }
        return result;
    }

    public String parseStatementList() throws Exception{
        String res = "\t";
        consumeSpace();
        res += parseStatement() + "\n\t";
        consumeSpace();
        res += parseStatementListTail();
        return res;
    }

    public String parseStatementListTail() throws Exception{
        String res = "";
        consumeSpace();
        if(peek() != ')' && peek() != '}' && peek() != ']'){
            res += parseStatement() + "\n\t";
            consumeSpace();
            res += parseStatementListTail();
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
        res += match('}')+"\n";
        return res;
    }

    public String parseAssignment() throws Exception{
        consumeSpace();
        String result = "";
        result += parseToken() + " ";
        consumeSpace();
        result += match('=') + " ";
        consumeSpace();
        result += parseExpression();
        consumeSpace();
        result += match(';');



        return result;
    }

    public String parseGeneralFunctionCall() throws Exception{
        String res = "";
        consumeSpace();
        res += parseFactor();
        consumeSpace();
        res += match(';');
        return res;
    }

    public String parseWhileStatement() throws Exception{
        return null;
    }

    public String parseForStatement() throws Exception{
        return null;
    }

    public String parseIfStatement() throws Exception{
        String result = "";
        consumeSpace();
        result += parseReservedWord();
        consumeSpace();

        return result;
    }



    public String parseFunctionDeclaration() throws Exception{
        return null;
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
            throw new Exception("MetaStatementFormatException");
        }
    }

    public String parseExpression() throws Exception{
        String res = "";
        consumeSpace();
        if(peek() != '"'){
            res += parseTerm();
            consumeSpace();
            res += parseExpressionTail();
        }else{
            res += parseString();
        }
        return res;
    }

    public String parseExpressionTail() throws Exception{
        char c = peek();
        String result = "";
        if( c == '+' || c == '-'){
            result += " "+c+" ";
            cursor ++;
            consumeSpace();
            result += parseTerm();
            result += parseExpressionTail();
            return result;
        }else{
            return "";
        }
    }

    public String parseConditionalExpression() throws Exception{
        String res = "";
        consumeSpace();

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
        return head + tail;
    }

    public String parseTermTail() throws Exception{
        char c = peek();
        String result = "";
        if(c == '*' || c == '/' || c == '%'){
            result += c;
            cursor ++;
            result += parseFactor();
            result += parseTermTail();
            return result;
        }else{
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
            return parseNumber();
        }else if(isLetter(c)){
            String res = parseToken();
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
            throw new Exception("TokenFormatException");
        }
        while(isLegal(peek()) && cursor < input.length()){
            result += peek();
            cursor ++;
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
        }else if(here.startsWith("char")){
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
        return result;
    }

    public String match(char c) throws Exception{
        if(c == peek()){
            cursor ++;
            return c+"";
        }else{
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

        String input = "true && false";

        MyCompiler compiler = new MyCompiler(input);

        try {
            System.out.println(compiler.parseGeneralFunctionCall());
        }catch(Exception e){
            System.out.println("Throw expcetion: " + e.getMessage());
        }
    }
}
