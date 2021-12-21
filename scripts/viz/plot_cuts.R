library(tidyverse)
library(plotly)
library(RColorBrewer)
library(svglite)

source("viz/read_cuts.R")

first_halftime <- rbind(cutter$first, tobias$first, benedikt$first, felix$first) %>%
    mutate(across(name, factor, levels = c("Cutter", "Benedikt", "Tobias", "Felix")))

second_halftime <- rbind(cutter$second, tobias$second, benedikt$second, felix$second) %>%
    mutate(across(name, factor, levels = c("Cutter", "Benedikt", "Tobias", "Felix")))


create_plot <- function(df) {
    ggplot(df) +
        geom_rect(
            aes(
                xmin = start, xmax = end,
                ymin = 0, ymax = 1,
                fill = cam,
                text = paste0(start, "-", end, "\n", "Length: ", end - start)
            ),
            color = "black", alpha = 0.5
        ) +
        facet_grid(rows = vars(name)) +
        scale_y_continuous(breaks = c(), labels = c()) +
        theme() +
        scale_fill_brewer(palette = "Dark2") +
        labs(fill = "Kamera")
}

print(ggplotly(create_plot(first_halftime), tooltip = "text"))



ggsave(file = "plots/first.svg", plot = create_plot(first_halftime), dpi = 1000)
ggsave(file = "plots/second.png", plot = create_plot(second_halftime))