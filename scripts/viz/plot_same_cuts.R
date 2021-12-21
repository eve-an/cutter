library(tidyverse)
library(plotly)
library(RColorBrewer)
library(svglite)

source("viz/read_cuts.R")


clean_file <- function(csv) {
    read_csv(csv) %>%
        mutate(cam = "Übereinstimmung") %>%
        mutate(name = "Übereinstimmung") %>%
        mutate(len = 0) %>%
        mutate(video_id = -1) %>%
        mutate(offset = 0) %>%
        split_halftime(5000, 24800)
}

plot_same <- function(df) {
    ggplot(df) +
        geom_rect(
            aes(
                xmin = start, xmax = end,
                ymin = 0, ymax = 1,
                fill = cam
            ),
            color = "black", alpha = 0.5
        ) +
        facet_grid(rows = vars(name)) +
        scale_y_continuous(breaks = c(), labels = c()) +
        theme() +
        scale_fill_brewer(palette = "Dark2") +
        labs(fill = "Kamera")
}


tf_df <- clean_file("same_cuts/csv/tobias_felix.csv")
tb_df <- clean_file("same_cuts/csv/tobias_benedikt.csv")
bf_df <- clean_file("same_cuts/csv/benedikt_felix.csv")

ct_df <- clean_file("same_cuts/csv/cuts_tobias.csv")
cb_df <- clean_file("same_cuts/csv/cuts_benedikt.csv")
cf_df <- clean_file("same_cuts/csv/cuts_felix.csv")

for (halftime in c("first", "second")) {
    tf <- plot_same(rbind(tf_df[[halftime]], tobias[[halftime]], felix[[halftime]]))
    tb <- plot_same(rbind(tb_df[[halftime]], tobias[[halftime]], benedikt[[halftime]]))
    bf <- plot_same(rbind(bf_df[[halftime]], benedikt[[halftime]], felix[[halftime]]))

    ct <- plot_same(rbind(ct_df[[halftime]], cutter[[halftime]], tobias[[halftime]]))
    cb <- plot_same(rbind(cb_df[[halftime]], cutter[[halftime]], benedikt[[halftime]]))
    cf <- plot_same(rbind(cf_df[[halftime]], cutter[[halftime]], felix[[halftime]]))

    ggsave(paste0("same_cuts/plots/tobias_felix_", halftime, ".png"), tf)
    ggsave(paste0("same_cuts/plots/tobias_benedikt_", halftime, ".png"), tb)
    ggsave(paste0("same_cuts/plots/benedikt_felix_", halftime, ".png"), bf)

    ggsave(paste0("same_cuts/plots/cutter_tobias_", halftime, ".png"), ct)
    ggsave(paste0("same_cuts/plots/cutter_benedikt_", halftime, ".png"), cb)
    ggsave(paste0("same_cuts/plots/cutter_felix_", halftime, ".png"), cf)
}