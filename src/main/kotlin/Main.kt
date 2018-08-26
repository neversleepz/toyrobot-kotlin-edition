import java.awt.Point
import java.io.InputStream
import java.util.*
import java.util.logging.Logger
import kotlin.reflect.KFunction

fun main(args: Array<String>) {
    CommandLoop(System.`in`, Command.Companion::build).readCommands()
}


/** command loop is responsible for checking the input stream for new commands,
 *  delegating to the parser
 */
class CommandLoop(
        inputStream: InputStream,
        private val parser: (String) -> Command
) {
    private val scanner: Scanner = Scanner(inputStream)
    private val tabletop: Tabletop = Tabletop(5)
    companion object {
        val logger: Logger = Logger.getLogger(CommandLoop::class.simpleName)
    }

    fun readCommands() {
        while (scanner.hasNextLine()) {
            val input = scanner.nextLine().trim()
            val command = parser(input)
            eval(command)
        }
    }

    private fun eval(command: Command) {
        try {
            when (command) {
                is Place -> tabletop.place(command.x.toInt(), command.y.toInt(), command.facing.toDirection())
                is Move -> tabletop.move()
                is Left -> tabletop.left()
                is Right -> tabletop.right()
                is Report -> tabletop.report()
                is Exit -> finish()
            }
        } catch (t: Throwable) {
            logger.severe { "Error whilst running command $command - ${t.message}" }
        }
    }

    private fun finish() {
        println("Bye!")
        System.exit(0)
    }
}

private fun String.toDirection() = Direction.valueOf(trim().toUpperCase())

enum class Direction(val xIncrement: Int, val yIncrement: Int) {
    NORTH(0, 1),
    EAST(1, 0),
    SOUTH(0, -1),
    WEST(-1, 0)
}

sealed class Command {

    companion object {
        private val logger = Logger.getLogger(Command::class.simpleName)
        fun build(command: String): Command {
            val words = command.split(" ")
            val cmd = words[0]
            val args = if (words.size >= 2) words.subList(1, words.size) else emptyList()
            val targetCommand = findCommand(cmd, args)

            return if (targetCommand == null) {
                Noop()
            } else {
                targetCommand.call(*args.toTypedArray()) as Command
            }
        }

        private fun findCommand(cmd: String, args: List<String>): KFunction<Any>? {
            val commands = listOf(Place::class, Move::class, Left::class, Right::class, Report::class, Exit::class)
            /** There is no way to enumerate sealed classes
            // Wait for this to be implemented => https://youtrack.jetbrains.com/issue/KT-14657 */
            val matchingCommand = commands
                    .find { kClass -> cmd.equals(kClass.simpleName, ignoreCase = true) }

            if (matchingCommand == null) {
                logger.warning { "Command not parsable - ignoring: '$cmd'" }
                return null
            }

            val constructor = matchingCommand.constructors.find { kFunction -> kFunction.parameters.size == args.size }
            if (constructor != null)
                return constructor
            else {
                logger.warning { "No command `$cmd` exists taking ${args.size} parameters" }
                return null
            }
        }

    }


}


data class Place(val x: String, val y: String, val facing: String) : Command()
class Move : Command()
class Left : Command()
class Right : Command()
class Report : Command()
class Noop : Command()
class Exit : Command()


data class Tabletop(val size: Int) {
    private lateinit var robotPosition: Point
    private lateinit var robot: Robot

    fun place(x: Int, y: Int, facing: Direction) {
        val location = Point(x, y)
        assertNotFallen(location)
        robot = Robot(facing)
        robotPosition = location
    }

    private fun assertNotFallen(location: Point) {
        val squareTableRange = 0 until size

        fun check(coordinate: Int) {
            check(coordinate in squareTableRange) {
                "Point $coordinate is out of range of the table of size $size" }
        }

        check(location.x)
        check(location.y)
    }

    fun move() {
        checkRobotPlaced()
        val newPosition: Point = robotPosition.clone() as Point
        val direction = robot.facing
        newPosition.translate(direction.xIncrement, direction.yIncrement)
        assertNotFallen(newPosition)

        robotPosition = newPosition
    }

    private fun checkRobotPlaced() {
        checkNotNull(robot) {"Robot not placed"}
        checkNotNull(robotPosition) { "Robot has no position" }
    }

    fun left() {
        checkRobotPlaced()
        robot.left()
    }

    fun right() {
        checkRobotPlaced()
        robot.right()
    }

    val logger: Logger = Logger.getLogger(Tabletop::class.simpleName)
    fun report() {
        checkRobotPlaced()
        logger.info("$robot, $robotPosition")
    }

}

data class Robot(var facing: Direction) {
    fun left() {
        turn(facing.ordinal.dec())
    }

    fun right() {
        turn(facing.ordinal.inc())
    }

    private fun turn(newOrdinal: Int) {
        var nextOrdinal = newOrdinal
        if (nextOrdinal < Direction.NORTH.ordinal) nextOrdinal = Direction.WEST.ordinal
        if (nextOrdinal > Direction.WEST.ordinal) nextOrdinal = Direction.NORTH.ordinal
        facing = Direction.values()[nextOrdinal]
    }
}