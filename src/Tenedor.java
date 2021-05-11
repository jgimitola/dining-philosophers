
import java.util.concurrent.Semaphore;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author JhanU
 */
public class Tenedor {

    //Modelo
    private final int id;
    private final Semaphore semaforo;

    //Grafico
    private int x;
    private int y;
    private double angulo;

    public Tenedor(int id, Semaphore semaforo) {
        this.id = id;
        this.semaforo = semaforo;
    }

    public Tenedor(int id, Semaphore semaforo, int x, int y, double angulo) {
        this.id = id;
        this.semaforo = semaforo;
        this.x = x;
        this.y = y;
        this.angulo = angulo;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public double getAngulo() {
        return angulo;
    }

    public void setAngulo(double angulo) {
        this.angulo = angulo;
    }

    public int getId() {
        return id;
    }

    public Semaphore getSemaforo() {
        return semaforo;
    }

}
