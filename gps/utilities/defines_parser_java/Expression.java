import java.util.ArrayList;
import java.util.HashMap;
import Exceptions.ExpressionError;
import Exceptions.ParserError;
import Exceptions.UnknownOperation;
import Exceptions.UnknownVariable;
import Exceptions.UnsupportedFunction;

import java.util.regex.*;

public class Expression
{
    private static final Pattern hexValue=Pattern.compile("^([A-Fa-f0-9]+)$");
    private static final Pattern number=Pattern.compile("^(\\d+(\\.\\d+)?)(e(-?\\d+))?$");
    private static final Pattern integer=Pattern.compile("^(\\d+'d)?(\\d+)(\\.0+)? *$");
    private static final Pattern decimal=Pattern.compile("^(\\d+\\.\\d*[1-9])0+ *$");
    
    private String value;
    private boolean evaluated;
    private TreeNode tree;
    
    public Expression(String expression) throws ParserError
    {
        tree=Parser.Parse(expression);
        evaluated=false;
    }
    
    public boolean IsReprint(){ return tree.GetType()==Tokenizer.TokenType.AT; }
    
    public String Value(HashMap<String,Expression> vars) throws ExpressionError
    {
        if(!evaluated)
        {
            value=Evaluate(tree,vars);
            evaluated=true;
        }
        return value;
    }

    private String Evaluate(TreeNode tree, HashMap<String,Expression> vars) throws ExpressionError
    {
        return Evaluate(tree,vars,false);
    }
    
    private String Evaluate(TreeNode tree, HashMap<String,Expression> vars, boolean forceValue) throws ExpressionError
    {
        //Convert left parameter to a double.
        String leftString="";
        double leftValue=0;
        boolean haveLeftValue=false;
        if(tree.GetLeft()!=null)
        {
            leftString=Evaluate(tree.GetLeft(),vars,true);
            if(IsDouble(leftString))
            {
                haveLeftValue=true;
                leftValue=FromDoubleString(leftString);
            }
        }
        
        //Convert right parameter to a double.
        String rightString="";
        double rightValue=0;
        boolean haveRightValue=false;
        if(tree.GetRight()!=null&&
           tree.GetRight().GetType()!=Tokenizer.TokenType.SEMICOLON)
        {
            try
            {
                rightString=Evaluate(tree.GetRight(),vars,true);
            }
            catch(UnknownVariable e)
            {
                Matcher m;
                if((m=hexValue.matcher(tree.GetRight().GetValue())).matches())
                {
                    rightString=m.group(1);
                }
                else throw e;
            }
            
            if(IsDouble(rightString))
            {
                haveRightValue=true;
                rightValue=FromDoubleString(rightString);
            }
        }

        boolean useValue=false;
        double value=0;
        String stringValue="";
        switch(tree.GetType())
        {
        case Tokenizer.TokenType.PLUS:
            if(haveLeftValue && haveRightValue)
            {
                useValue=true;
                value=leftValue+rightValue;
            }
            else stringValue=leftString+"+"+rightString;
            break;
        case Tokenizer.TokenType.MINUS:
            if(haveLeftValue && haveRightValue)
            {
                useValue=true;
                value=leftValue-rightValue;
            }
            else stringValue=leftString+"-"+rightString;
            break;
        case Tokenizer.TokenType.TIMES:
            if(haveLeftValue && haveRightValue)
            {
                useValue=true;
                value=leftValue*rightValue;
            }
            else stringValue=leftString+"*"+rightString;
            break;
        case Tokenizer.TokenType.DIVIDE:
            if(haveLeftValue && haveRightValue)
            {
                useValue=true;
                value=leftValue/rightValue;
            }
            else stringValue=leftString+"/"+rightString;
            break;
        case Tokenizer.TokenType.CARET:
            if(haveLeftValue && haveRightValue)
            {
                useValue=true;
                value=Math.pow(leftValue,rightValue);
            }
            else stringValue=leftString+"^"+rightString;
            break;
        case Tokenizer.TokenType.COLON:
            stringValue=leftString+":"+rightString;
            break;
        case Tokenizer.TokenType.CONST:
            stringValue=leftString+tree.GetValue()+rightString;
            break;
        case Tokenizer.TokenType.FUNCTION:
            if(haveRightValue ||
               tree.GetRight().GetType()==Tokenizer.TokenType.SEMICOLON)
            {
                useValue=true;
                value=EvalFunction(tree,vars);
            }
            else stringValue=tree.GetValue()+"("+rightString+")";
            break;
        case Tokenizer.TokenType.HEX:
            stringValue=tree.GetValue();
        case Tokenizer.TokenType.VARIABLE:
            if(tree.GetValue().charAt(0)=='`')stringValue=tree.GetValue();
            else if(!vars.containsKey(tree.GetValue()))throw new UnknownVariable(tree.GetValue());
            else stringValue=vars.get(tree.GetValue()).Value(vars);
            break;
        case Tokenizer.TokenType.VALUE:
            useValue=true;
            value=EvalValue(tree.GetValue());
            break;
        default: throw new UnknownOperation(tree.GetType());
        }

        if(useValue)
        {
            stringValue=String.format("%-1.15f",value);
            Matcher m;
            if((m=integer.matcher(stringValue)).matches())
            {
                stringValue=m.group(2);
            }
            else if((m=decimal.matcher(stringValue)).matches())
            {
                stringValue=m.group(1);
            }
        }
        else if(forceValue)
        {
            Matcher m;
            if((m=integer.matcher(stringValue)).matches())
            {
                stringValue=m.group(2);
            }
        }
        
        return stringValue;
    }

    private double EvalValue(String valueString)
    {
        double value=0;

        Matcher m;
        if((m=number.matcher(valueString)).matches())
        {
            try{ value=Double.parseDouble(m.group(1)); }
            catch(NumberFormatException e){}
            if(m.start(4)>=0)
            {
                value*=Math.pow(10,Integer.parseInt(m.group(4)));
            }
            return value;
        }
        else return 0;
    }

    private double EvalFunction(TreeNode tree, HashMap<String,Expression> vars) throws ExpressionError
    {
        TreeNode child=tree.GetRight();
        String function=tree.GetValue();

        if(child.GetType()!=Tokenizer.TokenType.SEMICOLON)
        {
            //Convert child parameter to a double.
            double childValue;
            childValue=FromDoubleString(Evaluate(child,vars,true));
            return EvalFunction(function,childValue);
        }
        else
        {
            ArrayList<Double> values=new ArrayList<Double>();
            
            //Convert right parameters to doubles.
            String rightString;
            double rightValue;
            do
            {
                rightString=Evaluate(child.GetLeft(),vars);
            
                if(IsDouble(rightString))
                {
                    rightValue=FromDoubleString(rightString);
                }
                else throw new ExpressionError("expected value for '"+rightString+"'.");

                values.add(rightValue);
                child=child.GetRight();
            }
            while(child.GetType()==Tokenizer.TokenType.SEMICOLON);

            //Evaluate last parameter.
            rightString=Evaluate(child,vars);
            if(IsDouble(rightString))
            {
                rightValue=FromDoubleString(rightString);
            }
            else throw new ExpressionError("expected value for '"+rightString+"'.");
            values.add(rightValue);

            if(function.equals("max"))
            {
                double val=values.get(0);
                for(int i=1;i<values.size();i++)val=values.get(i)>val ? values.get(i) : val;
                return val;
            }
            else if(function.equals("min"))
            {
                double val=values.get(0);
                for(int i=1;i<values.size();i++)val=values.get(i)<val ? values.get(i) : val;
                return val;
            }
            else throw new UnsupportedFunction(function);
        }
    }

    private double EvalFunction(String function, double value) throws UnsupportedFunction
    {
        if(function.equals("abs"))return value<0 ? -value : value;
        else if(function.equals("acos"))return Math.acos(value);
        else if(function.equals("asin"))return Math.asin(value);
        else if(function.equals("atan"))return Math.atan(value);
        else if(function.equals("ceil"))return Math.ceil(value);
        else if(function.equals("cos"))return Math.cos(value);
        else if(function.equals("exp"))return Math.exp(value);
        else if(function.equals("floor"))return Math.floor(value);
        else if(function.equals("ln"))return Math.log(value);
        else if(function.equals("log10"))return Math.log10(value);
        else if(function.equals("log2"))return Math.log(value)/Math.log(2);
        else if(function.equals("max_value"))return Math.pow(2,Math.floor(value))-1;
        else if(function.equals("max_width"))return Math.ceil(Math.log(Math.ceil(value+1))/Math.log(2));
        else if(function.equals("round"))return Math.floor(value+0.5);
        else if(function.equals("sin"))return Math.sin(value);
        else if(function.equals("sqrt"))return Math.sqrt(value);
        else if(function.equals("tan"))return Math.tan(value);
        else throw new UnsupportedFunction(function);
    }
    
    private boolean IsDouble(String value)
    {
        try
        {
            Double.parseDouble(value);
            return true;
        }
        catch(NumberFormatException e)
        {
            return false;
        }
    }
    
    private double FromDoubleString(String value)
    {
        try
        {
            return Double.parseDouble(value);
        }
        catch(NumberFormatException e)
        {
            return 0;
        }
    }
}