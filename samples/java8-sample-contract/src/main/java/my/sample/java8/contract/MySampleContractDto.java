package my.sample.java8.contract;

public class MySampleContractDto {
    private final String name;
    private final Integer age;

    public MySampleContractDto(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public Integer getAge() {
        return age;
    }
}
