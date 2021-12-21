library(tidyverse)
library(plotly)
library(RColorBrewer)
library(svglite)


clean_csv <- function(csv, df_name) {
    csv <- csv %>%
        mutate(cam = case_when(
            video_id == "MAH00791.MP4.mp4" ~ "Action Cam 1",
            video_id == "MAH00790.MP4.mp4" ~ "Action Cam 1",
            video_id == "2019_0503_152615_004.MP4.mp4" ~ "Action Cam 2",
            video_id == "2019_0503_150006_003.MP4.mp4" ~ "Action Cam 2",
            video_id == "14570001.MOV.mp4" ~ "Overview",
        )) %>%
        mutate(len = end - start)

    csv$name <- df_name

    return(csv)
}

split_halftime <- function(df, fist_halftime_start, first_halftime_end) {
    cuts_first_half <- df %>%
        filter(start > fist_halftime_start & end < first_halftime_end)

    cuts_second_half <- df %>%
        filter(end > first_halftime_end)

    halftimes <- list("first" = cuts_first_half, "second" = cuts_second_half)

    return(halftimes)
}



cutter <- clean_csv(read_csv("cuts/cuts.csv", col_types = "cii"), "Cutter") %>%
    split_halftime(5000, 24800)

tobias <- clean_csv(read_csv("cuts/tobias.csv", col_types = "cii"), "Tobias") %>%
    split_halftime(5000, 24800)

benedikt <- clean_csv(read_csv("cuts/benedikt.csv", col_types = "cii"), "Benedikt") %>%
    split_halftime(5000, 24800)

felix <- clean_csv(read_csv("cuts/felix.csv", col_types = "cii"), "Felix") %>%
    split_halftime(5000, 24800)