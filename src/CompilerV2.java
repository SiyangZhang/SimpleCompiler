import com.sun.corba.se.impl.io.TypeMismatchException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class CompilerV2 {
    public int count;

    String output;

    public static final String PREFIX = "local[";
    public static final String POSTFIX = "]";
    public static final String SEMICO = ";";

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

    Stack<Character> ops;
    Stack<String> exp;

    Map<String, String> map;

    boolean jumpWhiteSpace;

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
        this.ops = new Stack<>();

        this.exp = new Stack<>();

        this.output = "";
        this.map = new HashMap<>();
        this.jumpWhiteSpace = true;

    }


    public String compile() throws Exception{
        return null;
    }





    public String parseProgram() throws Exception{
        String result = "";
        String meta = parseMetaStatement();
        String vars = parseVariableDeclaration();
        String funcs = parseFunctionDeclaration();

        result += meta + vars + funcs;
        return result;
    }

    public String parseVariableDeclaration() throws Exception{

        boolean flag = true;
        String result = "";
        while(flag){
            try {
                String type = parseType() + " ";
                result += type;


                result += parseToken();


                if(peek() == '='){
                    result += " "+match('=') + " ";

                    ;
                    result += parseExpression();
                }


                result += match(';') + "\n";
            }catch(TypeMismatchException e){
                flag = false;
            }
        }
//        System.out.println(result);
        return result;
    }

    public String parseOneVarDeclare() throws Exception{
        String result = "";
        result += parseType() + " ";



        result += parseToken();


        if(peek() == '='){
            String mid = " "+match('=') + " ";

            ;
            result = parseExpression() + mid + result;
        }


        result += match(';');

        return result;
    }



    public String parseType() throws TypeMismatchException,Exception {

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
            try {
                result += parseAssignment();

                return result;
            }catch(Exception e){
                e.printStackTrace();
                System.out.println("+++++++++++++++++++++++");
                cursor = now;
            }


            result += parseGeneralFunctionCall();
            result += match(';');

        }

        output += result;
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

        res += "\n"+match('}')+"\n";
        return res;
    }

    public String parseAssignment() throws Exception{

        String result = "";

        if(isLegal(peek())){
            result += parseToken() + " ";

            if(peek() == '=') {
                result += match('=') + " ";
                result += parseExpression();
                result += match(';');
                output += result;
                return result;
            }else{
                String here = input.substring(cursor);
                if(here.startsWith("*=") || here.startsWith("+=") || here.startsWith("-=") || here.startsWith("/=") || here.startsWith("%=") ){
                    result += " " + freeMatch();
                    result += freeMatch() + " ";

                    result += parseExpression();
                    output += result;
                    return result;
                }else if(here.startsWith("++") || here.startsWith("--")){
                    result += " " + freeMatch() + freeMatch() + " ";
                    output += result;
                    return result;
                }
            }
        }else{
            if(peek() == '+'){
                result += match('+') + match('+') + parseToken();
                output += result;
                return result;
            }else if(peek() == '-'){
                result += match('-') + match('-') + parseToken();
                output += result;
                return result;
            }
        }

        throw new Exception("Wrong Assignment.");
    }

    public String parseGeneralFunctionCall() throws Exception{
        String res = "";

        res += parseToken();

        res += match('(');

        res += parseExpressionList();

        res += match(')');


        output += res;
        return res;
    }

    public String parseWhileStatement() throws Exception{

        String here = input.substring(cursor);
        String res = "";
        if(here.startsWith("while")){
            res += parseReservedWord();

            res += match('(');

            res += parseExpression();

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

            res += match('(');

            res += parseAssignment();

            res += parseConditionalExpression();

            res += parseAssignment();

            res += parseBlockStatements();
            return res;
        }else{
            throw new Exception("illegal word.");
        }

    }

    public String parseIfStatement() throws Exception{
        String result = "";
        String here = input.substring(cursor);

        if(here.startsWith("if")){

            result += parseReservedWord();


            result += match('(');

            result += parseExpression();

            result += match(')');

            result += parseBlockStatements();

            here = input.substring(cursor);
            if(here.startsWith("else")){
                result += parseReservedWord();

                result += parseBlockStatements();

            }
        }else{
            throw new Exception("fail to parse If.");
        }

        return result;
    }

    public String parseReturnStatement() throws Exception{

        String result = "";
        String here = input.substring(cursor);
        if(here.startsWith("return")){
            result += parseReservedWord();

            if(peek() != ';'){
                result += parseExpression();
            }
            result += match(';');

            return result;
        }else{
            throw new Exception("Wrong Return Call.");
        }
    }


    public String parseBreakStatement() throws Exception{

        String result = "";
        String here = input.substring(cursor);
        if(here.startsWith("break")){
            result += parseReservedWord();

            result += match(';');

            return result;
        }else{
            throw new Exception("Wrong Break Call.");
        }
    }

    public String parseContinueStatement() throws Exception{

        String result = "";
        String here = input.substring(cursor);
        if(here.startsWith("continue")){
            result += parseReservedWord();

            result += match(';');

            return result;
        }else{
            throw new Exception("Wrong Continue Call.");
        }
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

            parseTerm();

            parseExpressionTail();

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
            freeMatch();


            String term = parseTerm();
            String right = exp.pop();
            String left = exp.pop();


            String notation = PREFIX + count++ + POSTFIX;
            exp.push(notation);
            char tok = ops.pop();
//            System.out.println(notation + " = " + left + " " + tok + " " + right +";");
            output += notation + " = " + left + " " + tok + " " + right +";\n";
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

        if(peek() == ')'){
            return "";
        }
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
            freeMatch();
            String factor = parseFactor();

            String right = exp.pop();
            String left = exp.pop();

            String equationleft = PREFIX + count++ + POSTFIX;
            exp.push(equationleft);
            char tok = ops.pop();
//            System.out.println(equationleft + " = " + left + " " + tok + " " + right +";");
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


//            return res;
            String notation = "local["+ count++ +"]";
            exp.push(notation);
//            System.out.println( notation + " = " + res+";");
            this.output += notation + " = " + res+";\n";
            return notation;

        }else if(isLetter(c)){


            String res = parseToken();
//
            if(peek() != '('){
                output += res;
                return res;
            }else{
                res += match('(');

                if(peek() != ')'){
                    res += parseExpressionList();

                }
                res += match(')');



                output += augmentArray() + " = " + res;

                return res;
            }



        }else if(c == '('){


            String result = "(";
            freeMatch();
            result += parseExpression();
            c = peek();
            if(c == ')'){
                freeMatch();
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
            result += freeMatch();
        }else{
            throw new Exception("TokenFormatException");
        }
        while(isLegal(peek()) && cursor < input.length()){
            result += freeMatch();
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
                result += freeMatch();
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

        if(jumpWhiteSpace) consumeSpace();
        if(c == peek()){
            cursor ++;
            if(jumpWhiteSpace) consumeSpace();
            System.out.println("match: " + c);
            return c+"";
        }else{
            throw new Exception("MisMatchedException: expect "+c+", but see "+peek()+".");
        }
    }

    public String freeMatch() throws Exception{
        System.out.print("free ");
        String c = match(peek());
        return c;
    }

    public void consumeSpace(){
        char c = peek();
        while(c == ' ' || c == '\n' || c == '\t'){
            cursor ++ ;
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

    public String augmentArray(){
        return PREFIX + count++ + POSTFIX;
    }





    public static void main(String[] args) {

        double[] local = new double[1000];







        try{


            Path path = Paths.get("./src/test.txt");
            List<String> inputs = Files.readAllLines(path);
            String input = "";
            for(int i = 0; i < inputs.size(); i++){
                input += inputs.get(i) + "\n";
            }
            CompilerV2 compiler = new CompilerV2(input);
            System.out.println(input);
            String output = compiler.parseStatement();
            System.out.println("------------------------------------------------");
            System.out.println(compiler.output);




        }catch(Exception e){
            System.out.println("Throw expcetion: " + e.getMessage());
            e.printStackTrace();
        }


    }
}
