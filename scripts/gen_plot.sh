echo "GENERATING DATA FOR PLOTS"
python data/mlt_to_csv.py
python data/same_cuts.py

echo "GENERATING PLOTS"
Rscript viz/plot_cuts.R
Rscript viz/plot_same_cuts.R