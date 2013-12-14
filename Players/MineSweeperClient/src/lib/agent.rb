 require_relative './board'
 # The intelligent agent that makes decisions based on the state of the board.
class Agent
  BOARD_STATUS = "BE"
  SCORE = "SCORE"
  EMPTY_CELL = "E"
  COVERED_CELL = "C"
  FLAGGED_CELL = "F"
  NUMERIC_CELL = /\d/
  UNCOVERED_CELL = /\d|E/
  UNCOVER_COMMAND = "UN"
  SET_FLAG_COMMAND = "SF"
  REMOVE_FLAG_COMMAND = "RF"

  # @!attribute [rw] board
  #   @return [Board] an object containing the game's cells.
  # @!attribute [r] last_play
  #   @return [Play] the last non-random play sent by the agent to the server.
  # @!attribute [r] last_message
  #   @return [Array] the last message received from the server.
  # @!attribute [r] next_plays
  #   @return [Array<Play>] a queue of plays to be sent to the server.
  # @!attribute [r] numeric_cells
  #   @return [Array<Play>] an array of empty plays that contain the coordinates
  #     of the cells wich content is a number.
  # @!attribute [rw] confirmed
  #   @return [Boolean] used to confirm the placement of a flag
  # @!attribute [rw] score
  #   @return [Board] the number of unflagged cells left in the board.
  attr_accessor :board, :last_message, :last_play, :next_plays, :numeric_cells

  # Initializes the instance variables for the agent.
  # @param logger [Logger]
  def initialize(logger)
    @logger = logger
    @board = nil
    @last_message = nil
    @last_play = nil
    @next_plays = []
    @numeric_cells = []
    @confirmed = nil
    @score = 0
  end

  # The main logic of the agent.
  #   After updating the state of the game the agent checks if it needs to repeat
  #   a play (due to network issues) and also checks if it needs to undo a set flag
  #   command (due to a logic error). Then it executes any play stored in the queue,
  #   if there are none, it checks if is possible to set a flag and if this is not possible
  #   it generates a random play.
  # @param message [Array] the last decoded message from the server.
  # @return [String] a play to be sent to the server.

  def play(message)
    @last_message = message
    set_attributes
    @logger.info "Agent: My Board looks like this:\n#{@board.to_s}"
    if repeat_last_play?
      return @last_play.to_command
    elsif undo_last_play?
      return @last_play.to_command
    elsif @last_play
      @logger.debug "I didn't have to repeat the play."
      case @board.cell(@last_play.coords)
      when EMPTY_CELL #comment if server is updated
        modify_neighbours UNCOVER_COMMAND
      when NUMERIC_CELL
        @logger.debug "Agent: Numeric cell found"
      end
    end

    # if commented, the performance may be improved buy it
    # may repeat the enemy moves
    clean_next_plays!

    unless @next_plays.empty?
      @logger.debug "Returning next play in queue"
      @last_play = @next_plays.last
      return @next_plays.pop.to_command
    end

    if @last_play
      if can_set_flags? and @next_plays.size > 0
          @last_play = @next_plays.last
          return @next_plays.pop.to_command
      else
        @logger.debug "There is no rational thing to do"
      end
    end

    @logger.debug "Sending random play"
    return random_play
  end

  # Set the state of the agent for the current cycle based on the last message received.
  def set_attributes
    command = @last_message[0]
    case command
    when BOARD_STATUS
      set_board
    end
  end

  # Search the board for numeric cells.
  def set_numeric_cells
    @numeric_cells = []
    @board.each do |cell,x,y|
      @numeric_cells << Play.new(x,y,nil) if cell =~ /\d/
    end
  end

  # Determines if the last sent play was completed and repeats it if necessary.
  def repeat_last_play?
    return false if @last_play.nil?
    @logger.debug("Ill test if i need to repeat #{@last_play.to_command}")
    x = @last_play.x
    y = @last_play.y
    cmd = @last_play.command
    @logger.debug("x:#{x} y:#{y} cell:#{@board.cell(x,y)}")
    unless command_matches_state?(cmd,@board.cell(x,y))
      @logger.debug "Repeating command #{@last_play.to_command}"
      return true
    end
    return false
  end

  # Determines if the last set flag command was a mistake and undoes it if so.
  def undo_last_play?
    return false if @last_play.nil?
    if @last_play.command == SET_FLAG_COMMAND
      @logger.debug "I'll test if I need to undo  #{@last_play.to_command}"
      unless @set_flag_confirmed
        @last_play.command = REMOVE_FLAG_COMMAND
        @logger.debug "I'll undo the last play with #{@last_play.to_command}"
        return true
      else
        @logger.debug "I won't undo the last play"

        return false
      end
    end
  end

  # Removes already executed plays from the queue, this is implemented
  #  in case the enemy have sent the command in the last cycle.
  def clean_next_plays!
    @next_plays.select! do |p|
      if p.command == UNCOVER_COMMAND
        @board.cell(p.coords) == COVERED_CELL
      elsif p.command == SET_FLAG_COMMAND
        @board.cell(p.coords) == FLAGGED_CELL
      else
        false
      end
    end
  end

  # Determines if it is possible to set flags and/or uncover new cells as a
  #  consequence of setting flags. If it can be done, the same method will
  #  enqueue the corresponding plays.
  def can_set_flags?
    set_numeric_cells
    return false if @numeric_cells.empty?
    rejected = 0
    @numeric_cells.each do |p|
      cell = @board.cell(p.coords).to_i
      covered, uncovered, flagged = analyze_neighbours(p.x,p.y)
      if cell - flagged >= covered
        modify_neighbours(SET_FLAG_COMMAND,p.x,p.y,)
        @set_flag_confirmed = false
      elsif flagged == cell and uncovered > 0
        modify_neighbours(UNCOVER_COMMAND,p.x,p.y,)
      else
        rejected += 1
      end
    end
    if rejected == @numeric_cells.size
      return false
    else
      return true
    end
  end

  # Generates a new random play, based on the covered cells remaining.
  #  If @set_flag_confirmed is used at all, the agent will play more safely,
  #  sending SF commands instead of UN commands. It may be modified to play
  #  more agressively.
  def random_play
    x = 0
    y = 0
    loop do
      x = Random.rand(@board.width)
      y = Random.rand(@board.height)
      break if @board.cell(x,y) == COVERED_CELL or @last_play.nil?
    end
    @last_play = Play.new
    @last_play.x = x
    @last_play.y = y
    # If an agressive agent is wanted, remove the unless-else statement and uncomment
    # the following statement:
    #@last_play.command = UNCOVER_COMMAND
    unless @set_flag_confirmed.nil?
      @last_play.command = SET_FLAG_COMMAND
    else
      @last_play.command = UNCOVER_COMMAND
    end
    @last_play.to_command
  end

  # Returns an array with a quantitative analysis to the 8-conn neighbours of a given cell.
  # @return [Array<Fixnum>] The number of covered, uncovered and flagged adjacent cells.
  def analyze_neighbours(x,y)
    covered = uncovered = flagged = 0
    @board.each_neighbour(x,y) do |cell,nx,ny|
      case cell.to_s
      when COVERED_CELL
        covered += 1
      when UNCOVERED_CELL
        uncovered += 1
      when FLAGGED_CELL
        flagged += 1
      end
    end
    [covered, uncovered, flagged]
  end

  # Queues an alteration to the 8-conn neighbours of a cell.
  # @param command [String] the command to be applied.
  # @param x [Fixnum] the x coord of the cell.
  # @param y [Fixnum] the y coord of the cell.
  # @note A set flag command is executed before any uncover cell.
  def modify_neighbours(command = nil,x = @last_play.x, y = @last_play.y)
    @board.each_neighbour(x,y) do |cell,nx,ny|
      p = Play.new(nx,ny,command)
      unless @next_plays.include? p or cell != COVERED_CELL
        @logger.debug %{I will send #{command} to all the neighbours of
        #{x},#{y} because covered, uncovered, flagged:
        #{analyze_neighbours(x,y)}}
        @next_plays.unshift p if p.command == UNCOVER_COMMAND
        @next_plays.push p if p.command == SET_FLAG_COMMAND
      end
    end
  end

  # Updates the score and confirms the flag of a mine.
  def score=(score)
    @logger.info "Score updated, Mines left: #{score[1]}"
    @score = score[1]
    if @last_play
      if @last_play.command == SET_FLAG_COMMAND
        @set_flag_confirmed = true
      end
    end
  end

  # Reads the last message to set the board of the curren cycle.
  def set_board
    if @board.nil?
      @board = Board.new { |board| board.cells = @last_message[1][3]}
      @logger.info "Agent: h = #{@board.height}, w = #{@board.width}"
    elsif @last_message[1][4] != nil
      @board.mines = @last_message[1][4]
    end

    @board.cells = @last_message[1][3]
  end

  # Matches a cell state with a command.
  # @param command [String]
  # @param cell [String]
  # @return [Boolean]
  def command_matches_state?(command,cell)
    cell_state = cell
    case command
    when UNCOVER_COMMAND
      return true if cell_state != COVERED_CELL
    when SET_FLAG_COMMAND
      return true if cell_state != COVERED_CELL
    when REMOVE_FLAG_COMMAND
      return true if cell_state == COVERED_CELL
    end
    false
  end

end
