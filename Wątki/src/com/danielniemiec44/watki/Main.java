package com.danielniemiec44.watki;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

	    System.out.print("Podaj pierwsza liczbe: ");
	    long a = scanner.nextLong();
        System.out.print("Podaj druga liczbe: ");
	    long b = scanner.nextLong();

        Runnable obliczSume = new Runnable(){
            private volatile long suma;

            public long getResult() {
                return suma;
            }

            public void run(){
                suma = a + b;
            }
        };
        Thread watek1 = new Thread(obliczSume);
        watek1.start();

        long i = obliczSume.getResult();

        System.out.println();


    }

}
