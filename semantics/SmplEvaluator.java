package smpl.semantics;

import smpl.syntax.*;
import smpl.sys.SmplException;
import smpl.sys.TypeException;
import smpl.values.*;
import java.util.*;
import java.lang.Math;


public class SmplEvaluator implements Visitor<Environment, SmplValue> {

	protected SmplValue result;


	@Override
	public SmplValue visitSmplProgram(SmplProgram p, Environment env) throws SmplException {
		result = p.getSeq().visit(this, env);
		return result;
	}

	// statements

	@Override
	public SmplValue visitStmtSequence(StmtSequence sseq, Environment env) throws SmplException{
		ArrayList<Statement> seq = sseq.getSeq();
		result = SmplValue.make(0); // defaut result
		for (Statement s : seq){
			result = s.visit(this, env);
		}
		// return last evaluated value
		return result;
	}

	@Override
	public SmplValue visitStmtDefinition(StmtDefinition sd, Environment env) throws SmplException{

		if(sd.getVectorReference() == null){
			// assign values to variables
			ArrayList<Exp> args = sd.getExps();
			ArrayList<String> vars = sd.getVars();

			int a_size = args.size();
			int v_size = vars.size();

			if(a_size == 1 && v_size != 1){
				Exp e = args.get(0);
				result = e.visit(this,env);
				if(result.getType() == SmplTypes.LIST){
					SmplList l = result.listValue();
					for(int i=0; i<v_size; i++){
						if(l.getNextValue() != null){
							env.put(vars.get(i), l.getCurrentValue());
							l = l.getNextValue();
						}
					}
				}
			} else if(a_size != v_size){
				throw new SmplException("Must assign same number of expressions as variables");
			} else {
				for(int i=0; i<a_size; i++)
					env.put(vars.get(i), args.get(i).visit(this, env));
			}
		} else {
			// assign value to vector position
			// get vector reference
			ExpVectorRef vr = sd.getVectorReference();
			// get value to assign
			Exp val = sd.getExp();
			// get vector and position
			String vecVar = vr.getVar();
			Exp ref = vr.getRef();
			result = ref.visit(this, env);
			// confirm vector position is int
			if(result.getType() != SmplTypes.INTEGER && result.getType() != SmplTypes.REAL)
				throw new SmplTypeException(SmplTypes.INTEGER, result.getType());
			// get ref as int
			int _ref = result.intValue();
			// get vector
			SmplValue vec = env.get(vecVar);
			// confirm vector type
			if(vec.getType() != SmplTypes.VECTOR)
				throw new SmplTypeException(SmplTypes.VECTOR, vec.getType());
			// get list from vector class
			ArrayList lst = ((ExpVector)vec).getList();
			if(_ref < 0 || _ref >= lst.size())
				throw new SmplException("Reference to index [" + _ref + "] outside of bounds of " + vecVar + "[" + lst.size() + "]");
			lst.set(_ref, val.visit(this, env));

		}

		return SmplValue.make(true);
	}

	@Override
	public SmplValue visitStmtLet(StmtLet let, Environment env) throws SmplException{
		ArrayList<Binding> bindings = let.getBindings();
		Exp body = let.getBody();

		int size = bindings.size();
		String[] vars = new String[size];
		SmplValue[] vals = new SmplValue[size];
		Binding b;

		for (int i = 0; i < size; i++) {
		    b = bindings.get(i);
		    vars[i] = b.getVar();
		    // evaluate each expression in bindings
		    result = b.getValExp().visit(this, env);
		    vals[i] = result;
		}
		// create new env as child of current
		Environment newEnv = new Environment<> (vars, vals, env);
		return body.visit(this, newEnv);
	}

	@Override
	public SmplValue visitStmtPrint(StmtPrint sp, Environment env) throws SmplException{
		result = sp.getExp().visit(this, env);
		System.out.print(result.toString() + sp.getTerminatingCharacter());
		return result;
	}

	@Override
	public SmplValue visitExpAdd(ExpAdd exp, Environment env) throws SmplException {
		SmplValue lval, rval;
		lval = exp.getExpL().visit(this, env);
		rval = exp.getExpR().visit(this, env);
		return lval.add(rval);
	}

	@Override
	public SmplValue visitExpSub(ExpSub exp, Environment env) throws SmplException {
		SmplValue lval, rval;
		lval = exp.getExpL().visit(this, env);
		rval = exp.getExpR().visit(this, env);
		return lval.sub(rval);
	}

	@Override
	public SmplValue visitExpMul(ExpMul exp, Environment env) throws SmplException {
		SmplValue lval, rval;
		lval = exp.getExpL().visit(this, env);
		rval = exp.getExpR().visit(this, env);
		return lval.mul(rval);
	}

	@Override
	public SmplValue visitExpDiv(ExpDiv exp, Environment env) throws SmplException {
		SmplValue lval, rval;
		lval = exp.getExpL().visit(this, env);
		rval = exp.getExpR().visit(this, env);
		return lval.div(rval);
	}

	@Override
	public SmplValue visitExpMod(ExpMod exp, Environment env) throws SmplException {
		SmplValue lval, rval;
		lval = exp.getExpL().visit(this, env);
		rval = exp.getExpR().visit(this, env);
		return lval.mod(rval);
	}

	@Override
	public SmplValue visitExpPow(ExpPow exp, Environment env) throws SmplException {
		SmplValue lval, rval;
		lval = exp.getExpL().visit(this, env);
		rval = exp.getExpR().visit(this, env);
		return lval.pow(rval);
	}

	@Override
	public SmplValue visitExpLit(ExpLit exp, Environment env) throws SmplException {
		return exp.getVal();
	}

	@Override
	public SmplValue visitExpVar(ExpVar exp, Environment env) throws SmplException {
		return env.get(exp.getVar());
	}

	@Override
	public SmplValue visitExpProc(ExpProc proc, Environment env) throws SmplException {
		return new SmplProcedure(proc, env);
	}

	

	@Override
	public SmplValue visitExpPair(ExpPair exp, Environment env) throws SmplException {
		SmplValue v1 = exp.getExpL().visit(this, env);
		SmplValue v2 = exp.getExpR().visit(this, env);
		if(v2.getType() == SmplTypes.LIST || v2.getType() == SmplTypes.EMPTYLIST)
			return SmplValue.makeList(v1,(SmplList)v2);
		else
			return SmplValue.makePair(v1, v2);
	}

	@Override
	public SmplValue visitExpList(ExpList exp, Environment env) throws SmplException {
		ArrayList vals = new ArrayList();
		ArrayList<Exp> list = exp.getList();

		for(Exp lexp : list)
			vals.add(lexp.visit(this, env));

		return SmplValue.makeList(vals);
	}

	@Override
	public SmplValue visitExpVector(ExpVector exp, Environment env) throws SmplException {

		ArrayList<Exp> lst = exp.getList();
		ArrayList vals = new ArrayList();

		for(Exp e : lst){
			result = e.visit(this, env);
			if(result.getType() == SmplTypes.SUBVECTOR){
				int size = ((SmplSubVector)result).getSizeInt();
				SmplProcedure proc = ((SmplSubVector)result).getProcedure();
				ExpProc ExpProc = proc.getExpProc();
				// get parameters and expression body
				ArrayList<String> params = new ArrayList(ExpProc.getParameters());
				if(params.size() > 1 || ExpProc.getListVar() != null)
					throw new SmplException("Procedure must have 1 or no parameters.");
				Exp body = ExpProc.getBody();
				// evaluate for 0 through size
				for(int i=0; i<size; i++){
					ArrayList<SmplInt> args = new ArrayList();
					args.add(SmplValue.make(i));
					Environment newEnv = new Environment(params, args, env);
					vals.add(body.visit(this,newEnv));
				}
			} else {
				vals.add(e.visit(this, env));
			}
		}

		return SmplValue.makeVector(vals);
	}

	@Override
	public SmplValue visitExpVectorRef(ExpVectorRef exp, Environment env) throws SmplException {

		Exp ref = exp.getRef();
		result = ref.visit(this, env);

		if(result.getType() != SmplTypes.INTEGER && result.getType() != SmplTypes.REAL)
			throw new SmplTypeException(SmplTypes.INTEGER, result.getType());

		int _ref = result.intValue();

		String var = exp.getVar();
		SmplValue val = env.get(var);

		if(val.getType() != SmplTypes.VECTOR)
			throw new SmplTypeException(SmplTypes.VECTOR, val.getType());

		ArrayList lst = ((ExpVector)val).getList();

		if(_ref < 0 || _ref >= lst.size())
				throw new SmplException("Reference to index [" + _ref + "] outside of bounds of " + var + "[" + lst.size() + "]");

		return lst.get(_ref);
	}

	@Override
	public SmplValue visitExpSize(ExpSize exp, Environment env) throws SmplException {

		Exp body = exp.getBody();
		result = body.visit(this, env);

		if(result.getType() != SmplTypes.VECTOR)
			throw new SmplTypeException(SmplTypes.VECTOR, result.getType());

		ArrayList lst = ((ExpVector)result).getList();

		return SmplValue.make(lst.size());
	}

	
	@Override
	public SmplValue visitExpPairCheck(ExpPairCheck exp, Environment env) throws SmplException {
		Exp toCheck = exp.getExp();
		result = toCheck.visit(this, env);
		SmplTypes type = result.getType();

		return new SmplBoolean(type == SmplTypes.PAIR || type == SmplTypes.LIST || type == SmplTypes.EMPTYLIST);
	}

	@Override
	public SmplValue visitExpCar(ExpCar exp, Environment env) throws SmplException {
		// check that expression is a pair
		result = exp.getExp().visit(this, env);
		SmplTypes type = result.getType();

		if(type == SmplTypes.PAIR)
			return ((SmplPair)result).getFirstValue();
		else if(type == SmplTypes.LIST || type == SmplTypes.EMPTYLIST)
			return ((SmplList)result).getFirstValue();
		else
			throw new SmplTypeException(SmplTypes.PAIR, type);
	}

	@Override
	public SmplValue visitExpCdr(ExpCdr exp, Environment env) throws SmplException {
		// check that expression is a pair
		result = exp.getExp().visit(this, env);
		SmplTypes type = result.getType();

		if(type == SmplTypes.PAIR)
			return ((SmplPair)result).getSecondValue();
		else if(type == SmplTypes.LIST || type == SmplTypes.EMPTYLIST)
			return ((SmplList)result).getSecondValue();
		else
			throw new SmplTypeException(SmplTypes.PAIR, type);
	}

	@Override
	public SmplValue visitExpEqual(ExpEqual exp, Environment env) throws SmplException {
		SmplValue lval, rval;
		lval = exp.getExpL().visit(this, env);
		rval = exp.getExpR().visit(this, env);
		return lval.eq(rval);
	}

	@Override
	public SmplValue visitExpGreater(ExpGreater exp, Environment env) throws SmplException {
		SmplValue lval, rval;
		lval = exp.getExpL().visit(this, env);
		rval = exp.getExpR().visit(this, env);
		return lval.gt(rval);
	}

	@Override
	public SmplValue visitExpLess(ExpLess exp, Environment env) throws SmplException {
		SmplValue lval, rval;
		lval = exp.getExpL().visit(this, env);
		rval = exp.getExpR().visit(this, env);
		return lval.lt(rval);
	}

	@Override
	public SmplValue visitExpLessEqual(ExpLessEqual exp, Environment env) throws SmplException {
		SmplValue lval, rval;
		lval = exp.getExpL().visit(this, env);
		rval = exp.getExpR().visit(this, env);
		return lval.le(rval);
	}

	@Override
	public SmplValue visitExpGreatEqual(ExpGreatEqual exp, Environment env) throws SmplException {
		SmplValue lval, rval;
		lval = exp.getExpL().visit(this, env);
		rval = exp.getExpR().visit(this, env);
		return lval.ge(rval);
	}

	@Override
	public SmplValue visitExpNotEqual(ExpNotEqual exp, Environment env) throws SmplException {
		SmplValue lval, rval;
		lval = exp.getExpL().visit(this, env);
		rval = exp.getExpR().visit(this, env);
		return lval.ne(rval);
	}

	@Override
	public SmplValue visitExpLogicNot(ExpLogicNot exp, Environment env) throws SmplException {
		result = exp.getExp().visit(this, env);
		return result.not();
	}

	@Override
	public SmplValue visitExpLogicAnd(ExpLogicAnd exp, Environment env) throws SmplException {
		SmplValue lval, rval;
		lval = exp.getExpL().visit(this, env);
		rval = exp.getExpR().visit(this, env);
		return lval.and(rval);
	}

	@Override
	public SmplValue visitExpLogicOr(ExpLogicOr exp, Environment env) throws SmplException {
		SmplValue lval, rval;
		lval = exp.getExpL().visit(this, env);
		rval = exp.getExpR().visit(this, env);
		return lval.or(rval);
	}

	@Override
	public SmplValue visitExpBitNot(ExpBitNot exp, Environment env) throws SmplException {
		result = exp.getExp().visit(this, env);
		return result.bitnot();
	}

	@Override
	public SmplValue visitExpBitAnd(ExpBitAnd exp, Environment env) throws SmplException {
		SmplValue lval, rval;
		lval = exp.getExpL().visit(this, env);
		rval = exp.getExpR().visit(this, env);
		return lval.bitand(rval);
	}

	@Override
	public SmplValue visitExpBitOr(ExpBitOr exp, Environment env) throws SmplException {
		SmplValue lval, rval;
		lval = exp.getExpL().visit(this, env);
		rval = exp.getExpR().visit(this, env);
		return lval.bitor(rval);
	}

	@Override
	public SmplValue visitExpSubStr(ExpSubStr exp, Environment env) throws SmplException {

		String str = exp.getExpString().visit(this, env).stringValue();
		int lo = exp.getStartIndex().visit(this, env).intValue();
		int hi = exp.getEndIndex().visit(this, env).intValue();

		if(lo < 0 || lo > str.length())
			throw new SmplException("Starting index out of bounds");
		else if (hi > str.length())
			throw new SmplException("Ending index out of bounds");
		else if (hi < lo)
			return SmplValue.makeStr("");
		else
			return SmplValue.makeStr(str.substring(lo,hi));
	}

	@Override
	public SmplValue visitExpEqualv(ExpEqualv exp, Environment env) throws SmplException {

		SmplValue exp1 = exp.getExpFirst().visit(this, env);
		SmplValue exp2 = exp.getExpSecond().visit(this, env);

		if(exp1 == exp2)
		{
			return SmplValue.make(true);
		}else { return SmplValue.make(false); }
		
	}


	@Override
	public SmplValue visitExpCall(ExpCall exp, Environment env) throws SmplException {

		Exp expl = exp.getExpL();
		Exp expr = exp.getExpR();

		// confirm that first argument is a procedure
		SmplTypes expltype = expl.visit(this, env).getType();

		if(expltype != SmplTypes.PROCEDURE)
			throw new SmplTypeException(SmplTypes.PROCEDURE, expltype);

		// grab procedure
		SmplProcedure proc = (SmplProcedure) expl.visit(this, env);
		ExpProc toEval = proc.getExpProc();

		// get procedure parameters
		ArrayList<String> _params = new ArrayList(toEval.getParameters());
		int p_size = _params.size();

		// get procedure body
		Exp body = toEval.getBody();

		// confirm that second argument is a list
		SmplTypes exprtype = expr.visit(this, env).getType();

		if(exprtype != SmplTypes.LIST && exprtype != SmplTypes.EMPTYLIST)
			throw new SmplTypeException(SmplTypes.LIST, exprtype);

		// grab list
		SmplList lst = (SmplList) expr.visit(this, env);

		// convert to ArrayList
		ArrayList args = new ArrayList();
		ArrayList extras = new ArrayList();

		// ArrayList of parameters that are matched by arguments
		ArrayList<String> params = new ArrayList();

		// add value for each parameter,
		// create arraylist of extras,
		// add extras as argument
		int i = 0;	// counter
		while(lst.getType() != SmplTypes.EMPTYLIST){
			if(i < p_size){
				args.add(lst.getCurrentValue());
				params.add(_params.get(i));
			} else {
				extras.add(lst.getCurrentValue());
			}

			lst = lst.getNextValue();

			i++;	// increment counter
		}

		// get extra veriable
		String e = toEval.getListVar();
		if(e != null)
			params.add(e);

		// add extras to args
		if(!extras.isEmpty())
			args.add(SmplValue.makeList(extras));

		//System.out.println(args);
		Environment newEnv = new Environment(params, args, env);

		//System.out.println(newEnv);
		return body.visit(this, newEnv);

	}

	@Override
	public SmplValue visitExpLazy(ExpLazy exp, Environment env) throws SmplException {
		
		Boolean exists;

		try 
		{
		    env.get("101lazy");
		    exists = true;
		}catch(SmplException e) { exists = false; }

		if(!exists)
		{
			Exp body = exp.getExp();
			env.put("101lazy", body.visit(this, env));
		}else { result = env.get("101lazy"); }
		
		return result;

	}

	@Override
	public SmplValue visitExpDef(ExpDef exp, Environment env) throws SmplException {
		Exp body = exp.getExp();
		String var = exp.getVar();

		env.put(var, body.visit(this, env));

		return result;

	}

	
	@Override
	public SmplValue visitExpRead(ExpRead exp, Environment env) throws SmplException {

		Scanner input = new Scanner(System.in);
		result = SmplValue.makeStr(input.nextLine());
		return result;
	}

	@Override
	public SmplValue visitExpReadInt(ExpReadInt exp, Environment env) throws SmplException {
		
		Scanner input = new Scanner(System.in);
		if(input.hasNextInt()){
			result = SmplValue.make(input.nextInt());
			return result;
		} else {
			throw new SmplTypeException("Type Error: Input must be of type " + SmplTypes.INTEGER);
		}
	}

	@Override
	public SmplValue visitExpIf(ExpIf exp, Environment env) throws SmplException {
		
		Boolean conBool;
		if(exp.getElse())
		{
			Exp conExp = exp.getCondition();
			SmplValue conValue = conExp.visit(this, env);

			try{
				conBool = conValue.boolValue();
			}catch (Exception e){ throw new SmplException("Condition must evaluate to a boolean."); }
		
			if(conBool)
			{
				Exp ifArgBody = exp.getIfArg();
				result = ifArgBody.visit(this, env);
			}
			else
			{ 
				Exp elseArgBody = exp.getElseArg();
				result = elseArgBody.visit(this, env); 
			}
		}
		else
		{
			Exp conExp = exp.getCondition();
			SmplValue conValue = conExp.visit(this, env);

			try{
				conBool = conValue.boolValue();
			}catch (Exception e){ throw new SmplException("Condition must evaluate to a boolean."); }
			
			if(conBool)
			{
				Exp ifArgBody = exp.getIfArg();
				result = ifArgBody.visit(this, env);
			}
		}

		return result;

	}

	@Override
	public SmplValue visitExpCase(ExpCase exp, Environment env) throws SmplException {

		// get cases
		ArrayList<ExpPair> lst = exp.getList();
		Exp elseCond = null;
		// examine each case
		for(ExpPair _case : lst){
			Exp cond = _case.getExpL();
			SmplValue check = cond.visit(this, env);
			// skip evaluation for else condition
			if(check.getType() == SmplTypes.STRING){
				if(check.stringValue().equals("else")){
					elseCond = _case.getExpR();
					break;
				}
			}
			// check condition is booleans
			if(check.getType() != SmplTypes.BOOLEAN)
				throw new SmplTypeException(SmplTypes.BOOLEAN, check.getType());

			if(check.boolValue()){
				Exp body = _case.getExpR();
				result = body.visit(this, env);
				return result;
			}
		}
		// true case has not been found
		if(elseCond != null)
			result = elseCond.visit(this, env);
		// return value stored in result
		return result;
	}
	/**
	@Override
	public SmplValue visitExpSin(ExpSin exp, Environment env) throws SmplException {

		Exp param = exp.getExp();
		SmplValue value = param.visit(this, env);
		if(value.getType() == SmplTypes.INTEGER)
		{
			result = SmplValue.make(Math.sin(Double.valueOf(value.intValue())));
		}
		else if(value.getType() == SmplTypes.REAL)
		{
			result = SmplValue.make(Math.sin(value.doubleValue()));
		}
		
		return result;
	}
	@Override
	public SmplValue visitExpCos(ExpCos exp, Environment env) throws SmplException { 

		Exp param = exp.getExp();
		SmplValue value = param.visit(this, env);
		if(value.getType() == SmplTypes.INTEGER)
		{
			result = SmplValue.make(Math.cos(Double.valueOf(value.intValue())));
		}
		else if(value.getType() == SmplTypes.REAL)
		{
			result = SmplValue.make(Math.cos(value.doubleValue()));
		}
		return result;
	}
	@Override
	public SmplValue visitExpTan(ExpTan exp, Environment env) throws SmplException { 

		Exp param = exp.getExp();
		SmplValue value = param.visit(this, env);
		if(value.getType() == SmplTypes.INTEGER)
		{
			result = SmplValue.make(Math.tan(Double.valueOf(value.intValue())));
		}
		else if(value.getType() == SmplTypes.REAL)
		{
			result = SmplValue.make(Math.tan(value.doubleValue()));
		}
		return result;
	}
	@Override
	public SmplValue visitExpSec(ExpSec exp, Environment env) throws SmplException { 

		Exp param = exp.getExp();
		SmplValue value = param.visit(this, env);
		if(value.getType() == SmplTypes.INTEGER)
		{
			result = SmplValue.make( 1.0 / Math.cos(Double.valueOf(value.intValue())));
		}
		else if(value.getType() == SmplTypes.REAL)
		{
			result = SmplValue.make(1.0 / Math.cos(value.doubleValue()));
		}
		return result;
	}
	@Override
	public SmplValue visitExpCot(ExpCot exp, Environment env) throws SmplException { 

		Exp param = exp.getExp();
		SmplValue value = param.visit(this, env);
		if(value.getType() == SmplTypes.INTEGER)
		{
			result = SmplValue.make(   1.0/ Math.tan(Double.valueOf(value.intValue()))   );
		}
		else if(value.getType() == SmplTypes.REAL)
		{
			result = SmplValue.make( 1.0 / Math.tan(value.doubleValue()));
		}
		return result;
	}

	@Override
	public SmplValue visitExpCosec(ExpCosec exp, Environment env) throws SmplException { 

		Exp param = exp.getExp();
		SmplValue value = param.visit(this, env);
		if(value.getType() == SmplTypes.INTEGER)
		{
			result = SmplValue.make(1.0 / Math.sin(Double.valueOf(value.intValue())));
		}
		else if(value.getType() == SmplTypes.REAL)
		{
			result = SmplValue.make(1.0 / Math.sin(value.doubleValue()));
		}
		return result;
	}

	

	@Override
	public SmplValue visitExpDifferentiate(ExpDifferentiate exp, Environment env) throws SmplException { 
		if(exp.getForm())
		{
			Exp body = exp.getFunction().getBody();
			SmplQuadratic quad = SmplValue.makeQuadratic(body.toString());

			result =  SmplValue.makeStr(quad.differentiateThis());
		}
		else
		{
			SmplValue function = env.get(exp.getVar());
			if(function.getType() != SmplTypes.PROCEDURE)
			{
				throw new SmplTypeException(SmplTypes.PROCEDURE, function.getType());
			}
			else
			{
				SmplProcedure procedure = (SmplProcedure) function;
				Exp body = procedure.getExpProc().getBody();
				SmplQuadratic quad = SmplValue.makeQuadratic(body.toString());

				result =  SmplValue.makeStr(quad.differentiateThis());
			}
		}


		return result;
	}


	@Override
    public SmplValue visitExpPlot(ExpPlot exp, Environment env)throws SmplException {
    	
    	//temporary solution to displaying a given list
        ArrayList<SmplPair> display = new ArrayList<SmplPair>();
        Exp plot = exp.getExp();
        String[] vars = {exp.getVar()};
		


        int r1, r2;
        double[] inputs;

        //implementation adapted from fnplot to be used with a gui
        /*plotter.setPlotter(new GraphPlotter(gPanel));
        r1 =  exp.getLo().visit(this, env).doubleValue();
        r2 =  exp.getHi().visit(this, env).doubleValue();


        if(r1 > r2)
            inputs = plotter.sample(r2, r1);
        else
            inputs = plotter.sample(r1, r2);
        //returns lasts evaluations

        Point2D.Double[] pts = new Point2D.Double[inputs.length];
        for(int i = 0; i < inputs.length; i++){
            SmplValue[] vals = {SmplValue.make(inputs[i])};
            Environment newEnv = new Environment(vars, vals, env);
            result = plot.visit(this, newEnv);
            pts[i] = new Point2D.Double(inputs[i], result.doubleValue());

        }

        plotter.plot(pts);*/
	/**

        r1 =  exp.getLo().visit(this, env).intValue();
        r2 =  exp.getHi().visit(this, env).intValue();

        for(int i = r1; i < r2; i++ )
        {
        	SmplValue[] vals = {SmplValue.make(i)};
        	Environment newEnv = new Environment(vars, vals, env);
        	result = plot.visit(this, newEnv);
        	display.add(SmplValue.makePair(vals[0], result));
        }
        System.out.println(display.toString());
        return result;
    }

    @Override
	public SmplValue visitExpLimit(ExpLimit exp, Environment env) throws SmplException { 

		String[] vars = {exp.getVar()};

		env.put(exp.getVar(), exp.getApproach().visit(this, env));
		if(exp.getForm())
		{
			Exp body = exp.getFunction();
			
			

			
			result =  body.visit(this, env);
		}
		else
		{
			
			SmplValue function = env.get(exp.getFuncVar());
			if(function.getType() != SmplTypes.PROCEDURE)
			{
				throw new SmplTypeException(SmplTypes.PROCEDURE, function.getType());
			}
			else
			{
				SmplProcedure procedure = (SmplProcedure) function;
				Exp body = procedure.getExpProc().getBody();
				
				result =  body.visit(this, env);
			}

			
			



		}


		return result;
	}**/
}
