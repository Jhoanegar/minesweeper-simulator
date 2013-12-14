# Added features to the core Ruby library.

class String
	# Removes the first and last character of self.
  def remove_parenthesis
    self[1..-2]
  end
end
