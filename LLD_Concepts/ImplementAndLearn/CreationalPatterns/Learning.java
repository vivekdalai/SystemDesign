/**
 * How build() is created in a class.
 * you don't need to use setName(string) instead use
 * name(string) so it's more readable and useable while creating objects
 */
public class Learning {

    public static void main(String[] args) {
        BaseClass baseClass = new BaseClass.Builder()
                                .name("vivek")
                                .build();

        System.out.println(baseClass.toString());
    }

    public static class BaseClass {
        private String name;

        BaseClass(Builder b){
            this.name = b.name;
        }

        @Override
        public String toString(){
            return "name : " + this.name;
        }
        
        public static class Builder {
            private String name;

            public Builder name(String name){
                this.name = name;
                return this;
            }

            public BaseClass build() {
                return new BaseClass(this);
            }
        }
       
    }
    
}
