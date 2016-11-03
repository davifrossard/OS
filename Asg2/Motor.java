/**
 * Classe que representa os motores.
 * 
 * @author Davi E. N. Frossard, Rodolfo V. Valentim
 */
public class Motor implements Runnable {
	
	/**
	 * Contador da quantidade de objetos instanciados.
	 */
	private static int count = 0;
	
	/**
	 * Id do motor
	 */
	private int id;
	
	/**
	 * Fila de produtor/consumidor a ser consumida.
	 */
	private ProducerConsumer<MotorData<Integer,  Integer>> queueIn;
	
	/**
	 * Direção do passo atual.
	 */
	private int dir;

	/**
	 * Sinal de passo recebido
	 */
	private int pwm;
	
	/**
	 * Instancia um objeto da classe Motor.
	 * 
	 * @param ArdMot
	 * 		Fila de produtor/consumidor da qual o motor consumirá.
	 */
	public Motor(ProducerConsumer<MotorData<Integer, Integer>> ArdMot) {
		queueIn = ArdMot;
		id = ++count;
	}

	/**
	 * Método de execução da thread, simplesmente consome da fila de P/C
	 * e atualiza seu estado atual de acordo. 
	 * Delay de 1 segundo entre cada execução.
	 */
	@Override
	public void run() {
		while (true) {
			MotorData<Integer, Integer> res = null;
			try {
				Thread.sleep(1000);
				res = queueIn.get("Motor #"+id);
				dir = res.fst.intValue();
				pwm = res.snd.intValue();
			} catch (InterruptedException e) {
				System.out.println("Falha na interrupção!");
			}
		}
	}
	
	/**
	 * Método de obtenção da direção do passo mais recente.
	 * @return 
	 * 		A direção.
	 */
	public int getDir()
	{
		return dir;
	}

	/**
	 * Método de obtenção da quantidade de passos mais recente.
	 * @return 
	 * 		Os passos.
	 */
	public int getPos()
	{
		return pwm;
	}
}
