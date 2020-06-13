package converter

import java.util.Scanner

fun main() {
    println("Enter what you want to convert (or exit): ")
    val scanner = Scanner(System.`in`)
    var input: String = scanner.next()

    while (input.toLowerCase().trim() != "exit"){
        // application should exit when keyword is invoked.
        if (input.equals("exit", true)) break
        processInput(input, scanner)
        println("Enter what you want to convert (or exit): ")
        input = scanner.next()
    }

}

private fun processInput(input: String, scanner: Scanner) {
    val value: Double = input.toDouble()

    var unitFrom = scanner.next()
    if (unitFrom.toLowerCase().contains("degree")) {
        unitFrom += " " + scanner.next()
    }
    val connector = scanner.next() // store to discard

    var unitTo = scanner.next()
    if (unitTo.toLowerCase().contains("degree")) {
        unitTo += " " + scanner.next()
    }

    var enumUnitFrom = Units.getUnitInfo(unitFrom)
    val enumUnitTo = Units.getUnitInfo(unitTo)

    if (value < 0.0 && enumUnitFrom.unitType != "temperature" && enumUnitFrom.unitType != "???") {
        println("${enumUnitFrom.unitType.capitalize()} shouldn't be negative.")
        return
    }

    // case unit types are incompatible (convert m for kg)
    if (enumUnitFrom.unitType != enumUnitTo.unitType || enumUnitFrom == Units.OTHER || enumUnitTo == Units.OTHER) {
        println("Conversion from ${enumUnitFrom.pluralName} to ${enumUnitTo.pluralName} is impossible")
        return
    }

    // get the unit based on unit type
    val defaultUnit = getDefaultUnitFromUnitType(enumUnitFrom.unitType)
    var convertedValue: Double = 0.0
    // convertion's math
    if (enumUnitFrom.unitType.toLowerCase() == "temperature") {
        convertedValue = when {
            enumUnitFrom == Units.CELSIUS && enumUnitTo == Units.FAHRENHEIT -> convertC_F(value)
            enumUnitFrom == Units.CELSIUS && enumUnitTo == Units.KELVIN -> convertC_K(value)
            enumUnitFrom == Units.FAHRENHEIT && enumUnitTo == Units.CELSIUS -> convertF_C(value)
            enumUnitFrom == Units.FAHRENHEIT && enumUnitTo == Units.KELVIN -> convertF_K(value)
            enumUnitFrom == Units.KELVIN && enumUnitTo == Units.CELSIUS -> convertK_C(value)
            enumUnitFrom == Units.KELVIN && enumUnitTo == Units.FAHRENHEIT -> convertK_F(value)
            enumUnitFrom == enumUnitTo -> value
            else -> 0.0
        }
    } else {
        val valueInDefaultUnit = toDefaultUnit(value, enumUnitFrom)
        convertedValue = convertUnit(valueInDefaultUnit, enumUnitTo)
    }

    // get plural/singular for the output
    if (enumUnitFrom.unitType != "temperature") {
        val outputFrom = getOutputUnit(value, enumUnitFrom)
        val outputUTo = getOutputUnit(convertedValue, enumUnitTo)
        println("$value $outputFrom is $convertedValue $outputUTo\n")
    } else {
        unitFrom = Units.parseOutputStringForTemperature(unitFrom, value)
        unitTo = Units.parseOutputStringForTemperature(unitTo, convertedValue)
        println("$value $unitFrom is $convertedValue $unitTo\n")
    }

}

fun convertK_F(value: Double): Double {
    return (value * 9.0 / 5.0) - 459.67
}

fun convertK_C(value: Double): Double {
    return value - 273.15
}

fun convertF_K(value: Double): Double {
    return (value + 459.67) * 5.0 / 9.0
}

fun convertF_C(value: Double): Double {
    return (value - 32.0) * 5.0 / 9.0
}

fun convertC_K(value: Double): Double {
    return value + 273.15
}

fun convertC_F(value: Double): Double {
    return (value * 9.0 / 5.0) + 32.0
}


fun getDefaultUnitFromUnitType(unit: String): Units {
    return when (unit) {
        "length" -> Units.getUnitInfo("m")
        "weight" -> Units.getUnitInfo("g")
        "temperature" -> Units.getUnitInfo("c")
        else -> Units.getUnitInfo("XXX")
    }
}

fun getOutputUnit(value: Double, unit: Units): String {
    if (value.toInt() == 1) {
        return unit.singularName
    }
    else {
        return unit.pluralName
    }
}



fun convertUnit(value: Double, toUnit: Units): Double {
    return value * toUnit.multiplicationFactor
}
fun toDefaultUnit(value: Double, toUnit: Units): Double {
    return value / toUnit.multiplicationFactor
}

//fun toDefaultUnit(value: Double, fromUnit: String, defaultUnit: String): Double {
//    val defaultUnitInfo = Units.getUnitInfo(defaultUnit)
//    return value / defaultUnitInfo.multiplicationFactor
//}

enum class Units (val unit: String,
                  val singularName: String,
                  val pluralName: String,
                  val multiplicationFactor: Double,
                  val unitType: String,
                  val otherFormats: String = "") {
    METER("m", "meter", "meters", 1.0, "length"),
    KILOMETER("km", "kilometer", "kilometers", 0.001, "length"),
    CENTIMETER("cm", "centimeter", "centimeters", 100.0, "length"),
    MILLIMETER("mm", "millimeter", "millimeters", 1000.0, "length"),
    MILE("mi", "mile", "miles", 1609.35, "length"),
    YARD("yd", "yard", "yards", 1.094, "length"),
    FOOT("ft", "foot", "feet", 3.28084, "length"),
    INCH("in", "inch", "inches", 39.3701, "length"),
    GRAM("g", "gram", "grams", 1.0, "weight"),
    KILOGRAM("kg", "kilogram", "kilograms", 0.001, "weight"),
    MILLIGRAM("mg", "milligram", "milligrams", 1000.0, "weight"),
    POUND("lb", "pound", "pounds", (1.0/453.592) , "weight"),
    OUNCE("oz", "ounce", "ounces", (1.0/28.3495), "weight"),
    // temperature
    CELSIUS ("c", "degree Celsius", "degrees Celsius", (1.0), "temperature", "dc;degree Celsius;degrees Celsius;Celsius;C"),
    FAHRENHEIT ("f", "degree Fahrenheit", "degrees Fahrenheit", (1.0), "temperature", "df;degree Fahrenheit;degrees Fahrenheits;degrees Fahrenheit;degree Fahrenheits;F;Fahrenheit;Fahrenheits"),
    KELVIN ("k", "Kelvin", "Kelvins", (1.0), "temperature","K;Kelvin;Kelvins;dk;degree Kelvin;degree Kelvins; degrees Kelvins;degrees Kelvin"),
    // the unit that does not exist in this list
    OTHER("NA", "???", "???", 1.0, "???");
    
    companion object {
        fun getUnitInfo(input: String): Units {
            for (unit in Units.values()) {
                when (input.toLowerCase()) {
                    unit.unit.toLowerCase() ,
                    unit.pluralName.toLowerCase() ,
                    unit.singularName.toLowerCase() -> return unit
                }
            }
            return parseTemperature(input)
        }
        fun parseTemperature (input: String): Units {
            for (txt: String in Units.CELSIUS.otherFormats.split(";"))
                if (txt.toLowerCase() == input.toLowerCase()) return Units.CELSIUS
            for (txt: String in Units.FAHRENHEIT.otherFormats.split(";"))
                if (txt.toLowerCase() == input.toLowerCase()) return Units.FAHRENHEIT
            for (txt: String in Units.KELVIN.otherFormats.split(";"))
                if (txt.toLowerCase() == input.toLowerCase()) return Units.KELVIN
            return OTHER
        }

        fun parseOutputStringForTemperature (input: String, value: Double): String {
            for (txt: String in Units.CELSIUS.otherFormats.split(";")) {
                if (txt.toLowerCase().contains(input.toLowerCase())){
                    if (input.toLowerCase().contains("degree") || input.toLowerCase().contains("dc")) {
                        if (value == 1.0) return "degree Celsius"
                        if (value != 1.0) return "degrees Celsius"
                    }
                    if (value == 1.0) return "degree Celsius"
                    if (value != 1.0) return "degrees Celsius"
                }
            }
            for (txt: String in Units.FAHRENHEIT.otherFormats.split(";")) {
                if (txt.toLowerCase().contains(input.toLowerCase())){
                    if (input.toLowerCase().contains("degree") || input.toLowerCase().contains("df")) {
                        if (value == 1.0) return "degree Fahrenheit"
                        if (value != 1.0) return "degrees Fahrenheit"
                    }
                    if (value == 1.0) return "degree Fahrenheit"
                    if (value != 1.0) return "degrees Fahrenheit"
                }
            }
            for (txt: String in Units.KELVIN.otherFormats.split(";")) {
                if (txt.toLowerCase().contains(input.toLowerCase())){
                    if (input.toLowerCase().contains("degree") || input.toLowerCase().contains("dk")) {
                        if (value == 1.0) return "degree Kelvin"
                        if (value != 1.0) return "degrees Kelvin"
                    }
                    if (value == 1.0) return "Kelvin"
                    if (value != 1.0) return "Kelvins"
                }
            }
            return ""
        }
    }
}