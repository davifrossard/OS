/**
 * Classe tupla para ser utilizada nos comandos dos motores
 * 
 * @author Davi E. N. Frossard, Rodolfo V. Valentim
 *
 * @param <Y>
 * 		Tipo do primeiro valor.
 * @param <Z>
 * 		Tipo do segundo valor.
 */
public class MotorData<Y, Z> { 
  public final Y fst;
  public final Z snd;
  
  public MotorData(Y fst, Z snd) { 
    this.fst = fst; 
    this.snd = snd;
  } 
  
  @Override
  public String toString()
  {
	  return "<"+fst+", "+snd+">";
  }
} 