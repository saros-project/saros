module Jekyll
  module Tags
    # This class implements the Jekyll Block {% accordion <accordion id> %}.
    #
    # Adopted from http://mikelui.io/2018/07/22/jekyll-nested-blocks.html
    class AccordionTag < Liquid::Block
      # Block constructor that is called by Jekyll if
      # {% accordion <id> %} is parsed.
      # 
      # == Parameters:
      # tag_name::
      #   Name of the block (here "accordion")
      # accordion_id::
      #   Accordion id that is used by bootstrap to
      #   distinguish multiple accordions on one page.
      def initialize(tag_name, accordion_id, liquid_options)
        super
        if accordion_id.nil? || accordion_id.empty?
          raise 'Empty accordion id, please add an id: {% accordion <id> %}'
        end
        @accordionID = "accordion-#{accordion_id.delete ' '}"
      end

      def render(context)
        context.stack do
          context["accordionID"] = @accordionID
          context["collapsed_idx"] = 1
          @content = super
        end
        output = %(<div class="accordion mb-4" id="#{@accordionID}">#{@content}</div>)

        output
      end
    end
  end
end

Liquid::Template.register_tag('accordion', Jekyll::Tags::AccordionTag)
