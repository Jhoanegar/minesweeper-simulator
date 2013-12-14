# Handles the text to object conversion.
class Interpreter
  RESPONSE_REG = /^\(REG (OK|NO)\s?(P1|P2)?\)+/
  RESPONSE_GAME_STATE = /\(GE\s(\d)+\s(ON|SCORE|FIN)\s.*\)/
  RESPONSE_BOARD_STATE = /\((BE)\s(\d)+\s(\d)+\s(\d)+.*\)/
  COMMAND_UNCOVER = /\(UN\s(\d)+\s(\d)\)/

  # @!attribute response [r] contains the last command received from the server.

  attr_reader :response

  # Creates the Interpreter.
  # @param logger [Logger] the object to output the log.
  def initialize(logger)
    @logger = logger
  end
  # Creates a connect command with a default name.
  # @param player_name [String]
  # @note the player name should be 8 chars maximum.
  # @return [String]
  def connect_command(player_name="MICHIGAN")
    "(REG #{player_name[0...7]})"
  end
  # Transforms the message received by the server in an array that contains
  #  the name of the command in [0] and its contetns in [1].
  # @param message [String]
  def decode(message)
    case message
    when RESPONSE_REG
      if $1 == "OK"
        @response = $2
      elsif $1 == "NO"
        @response = nil
      end
    when RESPONSE_GAME_STATE
      if $2 == "ON"
        @response = ["ON",message.remove_parenthesis.split[3].to_i]
      elsif $2 == "SCORE"
        @response = ["SCORE",message.remove_parenthesis.split(" ")[3].to_i]
      elsif $2 == "FIN"
        @response = ["FIN",
            message.remove_parenthesis.split(" ")[-4..-1]]
      end

    when RESPONSE_BOARD_STATE
      @response = parse_board(message.remove_parenthesis)
    else
      @response = "UNKNOWN COMMAND"
    end
    @logger.debug "#{self.class}: I decoded #{@response}"
    @response
  end

  # Creates the response when the message is a (BE).
  # @param message [String] the portion of the message that contains
  #  the board state.
  # @return [Array<Array>]
  def parse_board(message)
    command, cycle, n_rows, n_cols,cells = message.split(" ",5)
    board = build_board(cells,n_rows.to_i,n_cols.to_i)
    return [command,[cycle.to_i,n_rows.to_i,n_cols.to_i,board]]
  end

  # Helper method to parse_board that builds the board..
  # @param cells <String> with only the portion of the message that has
  #   the actual board state.
  # @param rows <Fixnum> the number of rows in the board.
  # @param cols <Fixnum> the number of cols in the board.
  # @return <Array> the board in a bidimensional array.
  def build_board(cells,rows,cols)
    board = []
    cells = cells.split
    rows.times do |i|
      board << []
      cols.times do
        board[i] << cells.shift
      end
    end
    return board
  end

end

