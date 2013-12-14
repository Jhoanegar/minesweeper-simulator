require_relative './spec_helper'
describe Play do
  it 'should initialize correctly when sending params' do
    p = Play.new("2","3","UN")  
    p.x.should == 2
    p.y.should == 3
    p.command.should == "UN"
  end
  it 'should initialize correctly when sending a block' do
    play = Play.new do |p| 
      p.x = "2"
      p.y = "3"
      p.command = "UN"
    end
    play.x.should == 2
    play.y.should == 3
    play.command.should == "UN"
  end

  it 'should return itself as a command' do
    play = Play.new do |p|
      p.x = "2"
      p.y = "3"
      p.command = "UN"
    end
    play.to_command.should == "(UN 2 3)"
  end

  it 'should compare two plays correctly' do
    play1 = Play.new(2,3,"UN")
    play2 = Play.new(2,3,"UN")
    play3 = Play.new(2,3,"SF")
    play1.should == play2
    play2.should_not == play3

  end
end
