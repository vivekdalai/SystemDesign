public class PizzaOldImplementation {

    enum PizzaSize {
        SMALL,
        MEDIUM,
        LARGE
    }
    enum PizzaType {
        NY,
        Italian,
        Indian
    }
    
    PizzaSize size;
    PizzaType type;
    
    Pizza(String size, String type){
        if(size.toLowerCase().equals("small")){
            this.size = PizzaSize.SMALL;
        } else if (size.toLowerCase().equals("medium")) {
            this.size = PizzaSize.MEDIUM;
        } else if (size.toLowerCase().equals("large")) {
            this.size = PizzaSize.LARGE;
        }

        if(type.toLowerCase().equals("ny")){
            this.type = PizzaType.NY;
        } else if (type.toLowerCase().equals("italian")) {
            this.type = PizzaType.Italian;
        } else if (type.toLowerCase().equals("indian")) {
            this.type = PizzaType.Indian;
        }
    }
}
