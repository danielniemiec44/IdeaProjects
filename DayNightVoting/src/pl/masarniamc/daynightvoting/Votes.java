package pl.masarniamc.daynightvoting;

public enum Votes {
    DAY("dzień"), NIGHT("noc");
    String name;

    Votes(String name) {
        this.name = name;
    }

    @Override
    public String toString(){
        return name;
    }
}
