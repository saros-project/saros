# Liquid block for utilizing the Bootstrap alerts without
# writing HTML in the markdown files. 
module Jekyll
  class AlertTagBlock < Liquid::Block
    # Corresponding to: https://getbootstrap.com/docs/4.0/components/alerts/
    @@valid_types = ['primary', 'secondary', 'success', 'danger', 'warning', 'info', 'light', 'dark']

    # Block constructor that is called by Jekyll if
    # {% alert <alert type> %} is parsed.
    # 
    # == Parameters:
    # tag_name::
    #   Name of the block (here "alert")
    # type::
    #   Alert type (as defined by bootstrap)
    def initialize(tag_name, type, tokens)
        super
        @type = type.strip
        verify_type
    end

    def render(context)
      text = super
      "<div class='alert alert-#{@type}' role='alert'>#{text}</div>"
    end

    private

    # Verifies the specified alert type and raises an exception
    # if the type is not supported.
    def verify_type
        if ! @@valid_types.include? @type
            raise "Alert type #{@type} is not valid, please provide one of: #{@@valid_types}\ne.g. {% alert info %}"
        end
    end
  end
end

Liquid::Template.register_tag('alert', Jekyll::AlertTagBlock)
