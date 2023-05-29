package utility

class DoubleExtensions {
    companion object {
        fun Round(number: Double): Double {
            var roundedValue: Double = number
            var strVal: String = String.format("e12", number)
            roundedValue = strVal.toDouble()
            return roundedValue
        }

        fun Round(number: Double?): Double? {

            if (number == null){
                return number
            }

            var roundedValue: Double? = number
            var strVal: String = String.format("e12", number)
            roundedValue = strVal.toDouble()
            return roundedValue
        }
    }
}
