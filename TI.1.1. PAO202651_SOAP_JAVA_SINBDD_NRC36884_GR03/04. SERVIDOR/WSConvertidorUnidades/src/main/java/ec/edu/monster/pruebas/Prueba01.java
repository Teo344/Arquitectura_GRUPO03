/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.edu.monster.pruebas;

import ec.edu.monster.servicios.ConvertidorServicio;

/**
 *
 * @author MateoCriollo
 */
public class Prueba01 {
    public static void main(String[] args){
        //DATOS
        int n1 = 10;
        int n2 = 12;
        
        //proceso
        ConvertidorServicio servicio = new ConvertidorServicio();
        int suma = servicio.convertirTiempo(n1, n2);
        
        System.out.println("n1: "+n1);
        System.out.println("n2: "+n2);
        System.out.println("total: " + suma);
    }
}
