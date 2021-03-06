package sat.wooftown.io


import sat.wooftown.util.Clause
import sat.wooftown.util.Formula
import sat.wooftown.util.Variable
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import kotlin.math.abs

/**
 * Class which parse DIMACS files (*.cnf)
 * @param input - путь к файлу
 */
class Parser(
    private val input: String,
) {

    constructor(file: File) : this(file.toString())

    /**
     * Function which returns cnf in "Formula format"
     * @return Model and count of variables
     * @see sat.wooftown.util.Model
     */
    fun parse(): Pair<Formula, Int> {
        val formula = Formula()
        val reader = CNFReader(File(input))


        for (j in 0 until reader.numberOfClauses) {
            val clause = Clause()
            for (dnf in reader.readDNF()) {
                if (dnf == 0) {
                    formula * clause
                } else {
                    if (dnf > 0) {
                        clause + (+Variable(abs(dnf) - 1))
                    } else {
                        clause + (-Variable(abs(dnf) - 1))
                    }
                }
            }
        }
        reader.close()
        return formula to reader.numberOfVariables
    }

    fun getStats(): Pair<Int, Int> {
        return CNFReader(File(input)).numberOfVariables to CNFReader(File(input)).numberOfClauses
    }


    /**
     * Класс-помощник, наследующий буфферного читателя файла
     */
    private class CNFReader(file: File) : BufferedReader(FileReader(file)) {

        var numberOfVariables: Int
        var numberOfClauses: Int

        /*
        ищем первую строчку выражения для определения сколько у нас строк и переиенных
         */
        init {
            val properties = Regex("p cnf (\\d+)\\s+(\\d+)").find(readLine())
                ?: throw IllegalArgumentException("Wrong format for .cnf file")
            numberOfVariables = properties.groupValues[1].toInt()
            numberOfClauses = properties.groupValues[2].toInt()
        }

        /**
         * Прочитать строчку, комментарии не учитываются
         */
        override fun readLine(): String {
            while (true) {
                val nextLine = super.readLine() ?: throw IOException("Wrong format for .cnf file")

                // строчка-коммент или пустая
                if (nextLine.startsWith("c") || nextLine.isEmpty()) {
                    continue
                }
                return nextLine
            }
        }

        /*
            return one clause
            variables can be separated with any number of whitespace characters -> regex for it
         */
        fun readDNF(): List<Int> {
            return readLine().trim().split(Regex("\\s+")).map { it.toInt() }
        }
    }


}

