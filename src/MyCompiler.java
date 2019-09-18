import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MyCompiler {
    public int count;
    public static final String[] RESERVED_WORDS = {
            "int", "char", "if", "else", "while", "for", "break", "continue", "return", "true", "false", "switch", "case", "default"
    };

    public static final String[] COND_OPS = {
            "&&", "||", "<=", ">=", "&", "|", "<", ">", "==", "!="
    };
    public final char EOF = '啊';
    int cursor;
    String input;
    List<String> tokenlist;

    Stack<Character> stack;
    Stack<String> exp;

    boolean jumpWhiteSpace;

    public MyCompiler(String input){
        this.input = input+EOF;
        this.cursor = 0;
        this.tokenlist = new ArrayList<>();
        this.count = 0;
        this.stack = new Stack<>();
        stack.push('$');
        this.exp = new Stack<>();
        exp.push("base");
        jumpWhiteSpace = true;
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



        result += parseToken();


        if(peek() == '='){
            result += " "+match('=') + " ";

            ;
            result += parseExpression();
        }


        result += match(';');

        return result;
    }

    public String parseType() throws Exception{

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

        String res = "";
        res += parseType() + " ";

        res += parseToken();
        return res;
    }

    public String parseParamList() throws Exception{
        String res = "";

        res += parseParameter();

        res += parseParamListTail();
        return res;
    }

    public String parseParamListTail() throws Exception{
        String res = "";

        if(peek() == ',') {
            res += match(',') + " ";

            res += parseParameter();

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

        res += parseStatement() + "\n\t";

        res += parseStatementListTail();
        return res;
    }

    public String parseStatementListTail() throws Exception{
        String res = "";

        if(peek() != ')' && peek() != '}' && peek() != ']'){
            res += parseStatement() + "\n\t";

            res += parseStatementListTail();
        }
        return res;
    }

    public String parseBlockStatements() throws Exception{

        String res = "";
        res += match('{') + "\n\t";

        res += parseStatementList();

        res += match('}')+"\n";
        return res;
    }

    public String parseAssignment() throws Exception{

        String result = "";
        result += parseToken() + " ";

        result += match('=') + " ";

        result += parseExpression();

        result += match(';');



        return result;
    }

    public String parseGeneralFunctionCall() throws Exception{
        String res = "";

        res += parseFactor();

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

        result += parseReservedWord();


        return result;
    }



    public String parseFunctionDeclaration() throws Exception{
        return null;
    }

    public String parseFunctionCall() throws Exception{
        return null;
    }



    public String parseMetaStatement() throws Exception{


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

        if(peek() != '"'){
            res += parseTerm();

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

            result += parseTerm();
            result += parseExpressionTail();
            return result;
        }else{
            return "";
        }
    }

    public String parseConditionalExpression() throws Exception{
        String res = "";

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

        res += parseFactor();

        res += parseConditionalExpressionTail();

        return res;
    }



    public String parseExpressionList() throws Exception{
        String res = "";


        res += parseExpression();

        res += parseExpressionListTail();

        return res;
    }

    public String parseExpressionListTail() throws Exception{
        String res = "";

        if(peek() == ','){
            res += match(',') + " ";

            res += parseExpression();

            res += parseExpressionListTail();
        }
        return res;
    }

    public String parseTerm() throws Exception{

        String head = parseFactor();
        String tail = parseTermTail();

//        System.out.print("local[" + count +"] = " + exp.pop());
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

        String res = "";
        res += parseFactor();


        String here = input.substring(cursor);
        if(here.startsWith("&&")){
            res += " &&";
            cursor += 2;

            res += parseFactor();
        }else if(here.startsWith("&")){
            res += " &";
            cursor += 1;

            res += parseFactor();
        }else if(here.startsWith("||")){
            res += " ||";
            cursor += 2;

            res += parseFactor();
        }else if(here.startsWith(" |")){
            res += " |";
            cursor += 1;

            res += parseFactor();
        }


        return res;
    }


    public String parseFactor() throws Exception {

        char c = peek();
        if(isDigit(c)){
            String res = parseNumber();


            return res;

        }else if(isLetter(c)){


            String res = parseToken();
            if(peek() != '('){
                return res;
            }else{
                res += match('(');

                if(peek() != ')'){
                    res += parseExpressionList();

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
            if(jumpWhiteSpace) consumeSpace();
            cursor ++;
            if(jumpWhiteSpace) consumeSpace();
            System.out.println("match: " + c);
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


        try {
            Path path = Paths.get("./src/test.txt");
            List<String> inputs = Files.readAllLines(path);
            String input = "";
            for(int i = 0; i < inputs.size(); i++){
                input += inputs.get(i) + "\n";
            }
            System.out.println(input);
            MyCompiler compiler = new MyCompiler(input);
            String res = compiler.parseStatement();
            System.out.println("\n\n\n\n-----------------------------------------------------------\n" + res);
        }catch(Exception e){
            System.out.println("Throw expcetion: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
