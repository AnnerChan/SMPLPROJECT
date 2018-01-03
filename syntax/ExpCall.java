package smpl.syntax;

import smpl.semantics.Visitor;
import smpl.sys.SmplException;
import java.util.*;

public class ExpCall extends Exp {

  Exp exp, lst;
  ArrayList<Exp> lst;

  public ExpCall(Exp e){
    exp = e;
    lst = new ExpList();
  }

  public ExpCall(Exp e, ExpList l){
    exp = e;
    lst = l;
  }
  
  public ExpCall(Exp e, ArrayList l){
    exp = e;
    lst = l;
  }

  public Exp getExpL(){
    return exp;
  }

  public Exp getExpR(){
    return lst;
  }

  @Override
  public <S, T> T visit(Visitor<S, T> v, S arg) throws SmplException {
    return v.visitExpCall(this, arg);
  }

  @Override
  public String toString() {
    return "call(" + exp.toString() + ", " + lst.toString() + ")";
  }
}
