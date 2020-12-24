package pl.masarniamc.randomtp;

public class RandomNumber {

    public static int getInteger(int min, int max){
        return (int)(Math.random() * ((max - min) + 1)) + min;
    }

}
