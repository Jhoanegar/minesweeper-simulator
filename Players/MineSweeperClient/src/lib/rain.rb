require "curses"
include Curses
    
# Creates a raining animation using ncurces.h
class Rain  
  def ranf
    rand(32767).to_f / 32767
  end
  
  # Plays the animation
  def play  
    init_screen
    nl
    noecho
    srand
    
    xpos = {}
    ypos = {}
    message = {:str => "We've Won!\n",
               :y => lines/2, :x => (cols-15)/2}
    r = lines - 4
    c = cols - 4
    for i in 0 .. 4
      xpos[i] = (c * ranf).to_i + 2
      ypos[i] = (r * ranf).to_i + 2
    end
    time = 0
    i = 0
    until time > 40 
      setpos(message[:y],message[:x]); addstr(message[:str])
      
      x = (c * ranf).to_i + 2
      y = (r * ranf).to_i + 2
    
      setpos(y, x); addstr(".")
    
      setpos(ypos[i], xpos[i]); addstr("o")
    
      i = if i == 0 then 4 else i - 1 end
      setpos(ypos[i], xpos[i]); addstr("O")
    
      i = if i == 0 then 4 else i - 1 end
      setpos(ypos[i] - 1, xpos[i]);      addstr("-")
      setpos(ypos[i],     xpos[i] - 1); addstr("|.|")
      setpos(ypos[i] + 1, xpos[i]);      addstr("-")
    
      i = if i == 0 then 4 else i - 1 end
      setpos(ypos[i] - 2, xpos[i]);       addstr("-")
      setpos(ypos[i] - 1, xpos[i] - 1);  addstr("/ \\")
      setpos(ypos[i],     xpos[i] - 2); addstr("| O |")
      setpos(ypos[i] + 1, xpos[i] - 1); addstr("\\ /")
      setpos(ypos[i] + 2, xpos[i]);       addstr("-")
    
      i = if i == 0 then 4 else i - 1 end
      setpos(ypos[i] - 2, xpos[i]);       addstr(" ")
      setpos(ypos[i] - 1, xpos[i] - 1);  addstr("   ")
      setpos(ypos[i],     xpos[i] - 2); addstr("     ")
      setpos(ypos[i] + 1, xpos[i] - 1);  addstr("   ")
      setpos(ypos[i] + 2, xpos[i]);       addstr(" ")
    
      xpos[i] = x
      ypos[i] = y
      refresh
      sleep(0.1)
      time += 1
    end
  end
end
    
