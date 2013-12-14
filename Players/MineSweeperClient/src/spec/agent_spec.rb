require_relative 'spec_helper'

describe Agent do
  before(:each) do
    logger = double()
    logger.stub(:info)
    @logger = logger
  end

  it 'should set its board correctly' do
    agent =  Agent.new(@logger)
    message = ["BE",[10, 4, 3,
               [["00","01","02"],["10","11","12"],
                ["20","21","22"],["30","31","32"]]]]
    agent.send(:last_message=, message)
    agent.set_attributes
    agent.send(:board).cells.should == 
               [["00","01","02"],["10","11","12"],
                ["20","21","22"],["30","31","32"]]
  end

  it 'should get a random play the first time' do
    agent = Agent.new(@logger)
    message = ["BE",[10, 4, 3,
               [["00","01","02"],["10","11","12"],
                ["20","21","22"],["30","31","32"]]]]
    agent.play(message).should =~ Interpreter::COMMAND_UNCOVER
  end
 
  it 'should produce a random uncover within the given bounds' do
    agent = Agent.new(@logger)
    message = ["BE",[10, 4, 3,
               [["C","C","C"],["C","C","C"],
                ["C","C","C"],["C","C","C"]]]]
    play = agent.play(message)
    play.should =~ Interpreter::COMMAND_UNCOVER
  end

  it 'should uncover all the neighbours of an empty cell' do
    agent = Agent.new(@logger)
    agent.send(:last_play=,Play.new(1,1,"UN"))
    cells = [["C","C","C"],
             ["C","E","C"],
             ["C","C","C"]]
    expected_plays = ["(UN 0 0)", "(UN 0 1)", "(UN 0 2)",
                      "(UN 1 0)", "(UN 1 2)",
                      "(UN 2 0)", "(UN 2 1)", "(UN 2 2)"]
    board = Board.new
    board.cells = cells
    agent.send(:board=,board)
    agent.send(:modify_neighbours,"UN")
    agent.send(:next_plays).map(&:to_command).should =~ expected_plays
  end
  it 'should only uncover covered cells' do
    agent = Agent.new(@logger)
    agent.send(:last_play=,Play.new(1,1,"UN"))
    cells = [["E","C","C"],
             ["C","E","C"],
             ["C","C","P1F"]]
    
    expected_plays = ["(UN 0 1)", "(UN 0 2)",
                      "(UN 1 0)", "(UN 1 2)",
                      "(UN 2 0)", "(UN 2 1)"]
    board = Board.new
    board.cells = cells
    agent.send(:board=,board)
    agent.send(:modify_neighbours,"UN")
    agent.send(:next_plays).map(&:to_command).should =~ expected_plays
  end 

  it 'should uncover only adjacent cells' do
    agent = Agent.new(@logger)
    agent.send(:last_play=,Play.new(0,0,"UN"))
    cells = [["E","C","C"],
             ["C","E","C"],
             ["C","C","P1F"]]
    
    expected_plays = ["(UN 0 1)","(UN 1 0)"]
    board = Board.new
    board.cells = cells
    agent.send(:board=,board)
    agent.send(:modify_neighbours, "UN")
    agent.send(:next_plays).map(&:to_command).should =~ expected_plays
  end

  it 'should analyze correctly the neighbours' do
    agent = Agent.new(@logger)
    agent.send(:last_play=,Play.new(0,0,"UN"))
    cells = [["E","E","E"],
             ["E","1","1"],
             ["F","1","C"]]
    board = Board.new
    board.cells = cells
    agent.send(:board=,board)
    agent.send(:analyze_neighbours,1,1).should == [1,6,1]

  end

  it 'should flag the correct cells' do
    agent = Agent.new(@logger)
    agent.send(:last_play=,Play.new(1,1,"UN"))
    cells = [["E","1","1"],
             ["E","2","C"],
             ["E","2","C"]]
    
    expected_plays = [Play.new(2,2,"SF"),Play.new(2,1,"SF")]
    board = Board.new
    board.cells = cells
    agent.send(:board=,board)
    agent.send(:numeric_cells=,[Play.new(1,0,"SF"),
                                 Play.new(2,0,"SF"),
                                 Play.new(1,1,"SF"),
                                 Play.new(1,2,"SF")])
    agent.send(:can_set_flags?)
    agent.send(:next_plays).should =~ expected_plays 
  end

  it 'should not make mistakes' do
    agent = Agent.new(@logger)
    agent.send(:last_play=,Play.new(1,1,"UN"))
    cells = [["E","E","1","C"],
             ["E","1","2","C"],
             ["1","C","C","C"]]
    
    expected_plays = [Play.new(1,2,"SF")]
    board = Board.new
    board.cells = cells
    agent.send(:board=,board)
    agent.send(:numeric_cells=,[Play.new(2,0,"SF"),
                                 Play.new(1,1,"SF"),
                                 Play.new(2,1,"SF"),
                                 Play.new(0,2,"SF")])
    agent.send(:can_set_flags?)
    agent.send(:next_plays).should =~ expected_plays 
    end

  it 'should clean the plays based on the status of the board' do
    agent = Agent.new(@logger)
    cells = [["C","E","F","C"],
             ["C","E","2","C"],
             ["C","E","E","C"]]
    board = Board.new
    board.cells = cells
    agent.send(:board=,board)
    agent.send(:next_plays=,[Play.new(0,0,"UN"),Play.new(0,1,"UN"),
                             Play.new(0,2,"UN"),Play.new(1,0,"UN"),
                             Play.new(2,0,"SF"),Play.new(3,0,"SF"),
                             Play.new(1,1,"UN"),Play.new(1,2,"UN")])
    agent.send(:clean_next_plays!)
    agent.send(:next_plays).size.should == 4
  end
end
