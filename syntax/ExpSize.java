package smpl.syntax;

import smpl.semantics.Visitor;
import smpl.sys.SmplException;
import java.util.*;

public class ExpSize extends Exp {

  Exp body;

  public ExpSize(Exp body){
    this.body = body;
  }

  public Exp getBody(){
    return body;
  }

  @Override
  public <S, T> T visit(Visitor<S, T> v, S arg) throws SmplException {
    return v.visitExpSize(this, arg);
  }

  @Override
  public String toString() {
    return "size(" + body + ")";
  }
}

