module Jekyll
  module Tags
    # This class implements the Jekyll Block {% collapsible <heading> %} that is intended
    # to be used in an {% accordion <accordion id> %} tag.
    #
    # Adopted from http://mikelui.io/2018/07/22/jekyll-nested-blocks.html
    class CollapseTag < Liquid::Block
      HEADING_SYMBOL = '#'
      DEFAULT_HEADING = HEADING_SYMBOL * 5 + ' '

      # Block constructor that is called by Jekyll if
      # {% collapsible <title> %} is parsed.
      # 
      # == Parameters:
      # tag_name::
      #   Name of the block (here "collapsible")
      # title::
      #   Mardown title of the collapsible. If no
      #   heading is defined h5 is used.
      def initialize(tag_name, title, liquid_options)
        super
        if title.nil? || title.empty?
          raise 'Empty title, please add a title: {% collapsible <title> %}'
        end
        @title = title.strip
      end

      def render(context)
        # Get data defined by accordion and previous collapsible
        accordionID = context["accordionID"]
        idx = context["collapsed_idx"]

        collapsedID = "#{accordionID}-collapse-#{idx}"
        headingID = "#{accordionID}-heading-#{idx}"

        # increment for the next collapsible
        context["collapsed_idx"] = idx + 1

        site = context.registers[:site]
        converter = site.find_converter_instance(::Jekyll::Converters::Markdown)
        # Get content of block
        content = converter.convert(super)

        title_md = @title.strip
        title_md.prepend DEFAULT_HEADING unless title_md.start_with? HEADING_SYMBOL
        title_html = converter.convert(title_md)

        output = <<~EOS
          <div class="card" markdown="0">
            <div id="#{headingID}">
              <button class="btn btn-link text-left text-decoration-none collapsed" data-toggle="collapse"
                      data-target="##{collapsedID}" aria-expanded="false" aria-controls="#{collapsedID}">
                #{title_html}
              </button>
            </div>
            <div id="#{collapsedID}" class="collapse" aria-labelledby="#{headingID}" data-parent="##{accordionID}">
              <div class="card-body">#{content}</div>
            </div>
          </div>
        EOS

        output
      end
    end
  end
end

Liquid::Template.register_tag('collapsible', Jekyll::Tags::CollapseTag)
