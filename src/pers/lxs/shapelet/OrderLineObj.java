package pers.lxs.shapelet;


    public final class OrderLineObj implements Comparable<OrderLineObj> {

        public double distance;
        private double classVal;
        /**
         * Constructor to build an orderline object with a given distance and class value
         * @param distance distance from the obj to the shapelet that is being assessed
         * @param classVal the class value of the object that is represented by this OrderLineObj
         */
        public OrderLineObj(double distance, double classVal){
            this.distance = distance;
            this.classVal = classVal;
        }

        public double getDistance(){
            return this.distance;
        }

        public double getClassVal(){
            return this.classVal;
        }

        public void setDistance(double distance){
            this.distance = distance;
        }
        
        public void setClassVal(double classVal){
            this.classVal = classVal;
        }
        
        /**
         * Comparator for two OrderLineObj objects, used when sorting an orderline
         * @param o the comparison OrderLineObj
         * @return the order of this compared to o: -1 if less, 0 if even, and 1 if greater.
         */
        @Override
        public int compareTo(OrderLineObj o) {
            if(o.distance > this.distance){
                return -1;
            }else if(o.distance==this.distance){
                return 0;
            }
            return 1;
        }
    }

