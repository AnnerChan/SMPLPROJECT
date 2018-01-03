package smpl.values;

import smpl.sys.SmplException;
import smpl.semantics.Environment;
import smpl.syntax.ExpProc;
import static smpl.values.SmplValue.make;
import java.util.*;

public class SmplProcedure extends SmplValue {

	ExpProc procExp;
	Environment closingEnv;

	public SmplProcedure(ExpProc procExp, Environment closingEnv){
		this.procExp = procExp;
		this.closingEnv = closingEnv;
	}

	@Override
	public SmplTypes getType(){
		return SmplTypes.PROCEDURE;
	}

	public SmplProcedure procValue(){
		return this;
	}

	public ExpProc getProcExp(){
		return procExp;
	}

	public Environment getClosingEnv(){
		return closingEnv;
	}

	@Override
	public String toString() {
		String params;
		ArrayList<String> paramList = procExp.getParameters();
		String listvar = procExp.getListVar();
		int n = paramList.size();
		switch (n) {
			case 0: params = ""; break;
			case 1: params = paramList.get(0); break;
			default:
				params = paramList.get(0);
				for(int i=1; i<n; i++)
					params += ", " + paramList.get(i);
		}
		if(listvar != null)
			params += " . " + listvar;

		String body;

		if(procExp.getBody() != null)
			body = procExp.getBody().toString();
		else
			body = procExp.getExpressions().toString();

		return "[Procedure: (" + params + ") -> " + body + "]";
	}
}
