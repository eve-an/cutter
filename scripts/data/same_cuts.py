from sklearn.metrics import precision_recall_fscore_support
import os

class Cut:

    def __init__(self, offset, video_id, start, end) -> None:
        self.video_id = video_id
        self.offset = offset
        self.start = start
        self.end = end

    def pos_in_cut(self, pos):
        return pos >= self.start and pos <= self.end


class Video:

    def __init__(self, cuts, file) -> None:
        self.cuts = cuts
        self.file = file
    
    def file_without_ext(self):
        return self.file.split("/")[-1].split(".")[0]

    def max_frame(self):
        ends = [cut.end for cut in self.cuts]
        return max(ends)

    def min_frame(self):
        starts = [cut.start for cut in self.cuts]
        return min(starts)

    def expand_cuts(self):
        sorted_cuts = sorted(cuts, key=lambda cut: cut.start)
        ids = []
        for cut in sorted_cuts:
            for pos in range(cut.start, cut.end + 1):
                ids.append(cut.video_id)

        return ids

    def id_at_pos(self, pos):
        for cut in self.cuts:
            if cut.pos_in_cut(pos):
                return cut.video_id

        return "NONE"


def read_csv(csv_file):
    cuts = []
    with open(csv_file, "r") as csv:
        for line in csv.readlines()[1:]:
            vals = line.split(",")
            id = vals[0]
            offset = vals[1]
            start = int(vals[2])
            end = int(vals[3])

            cuts.append(Cut(id, offset, start, end))

    return Video(cuts, csv_file)


def extract_same_frames(csv_a, csv_b):
    same_frames = []
    for pos in range(csv_a.min_frame(), csv_a.max_frame() + 1):
        csv_a_id = csv_a.id_at_pos(pos)
        csv_b_id = csv_b.id_at_pos(pos)

        if csv_a_id == csv_b_id:
            if csv_a_id != "NONE":
                same_frames.append((pos, csv_a_id))
    
    return same_frames

def generate_same_cuts(csv_a, csv_b, outfile=None):
    print(f"Generate for {csv_a.file_without_ext()} and {csv_b.file_without_ext()}")
    if outfile is None:
        if os.path.exists("same_cuts/csv"):
            outfile = "same_cuts/csv/"
            outfile += csv_a.file_without_ext() + "_" + csv_b.file_without_ext() + ".csv"
        else:
            os.makedirs("same_cuts/csv")

    same_cuts = []
    same_frames = extract_same_frames(csv_a, csv_b)
    start_frame, cam = same_frames.pop(0)
    cut = Cut(0, cam, start_frame, start_frame)

    for frame, cam in same_frames:
        if frame - cut.end > 1:
            same_cuts.append(cut)
            cut = Cut(0, cam, frame, frame)
        else:
            cut.end += 1

    with open(outfile, "w") as csv:
        csv.write("start,end")
        for cut in same_cuts:
            csv.write("\n")
            csv.write(f"{cut.start},{cut.end}")

benedikt = read_csv("cuts/benedikt.csv")
tobias = read_csv("cuts/tobias.csv")
felix = read_csv("cuts/felix.csv")
cutter = read_csv("cuts/cuts.csv")


generate_same_cuts(tobias, felix)
generate_same_cuts(tobias, benedikt)
generate_same_cuts(benedikt, felix)

generate_same_cuts(cutter, felix)
generate_same_cuts(cutter, tobias)
generate_same_cuts(cutter, benedikt)


    # ground_truth = []
    # predicted = []

    # for pos in range(tobi.min_frame(), tobi.max_frame() + 1):
    #     gt_id = tobi.id_at_pos(pos)
    #     pd_id = cuts.id_at_pos(pos)

    #     if pd_id == "NONE":
    #         continue
    #         print(pos)

    #     ground_truth.append(gt_id)
    #     predicted.append(pd_id)

    # print(precision_recall_fscore_support(ground_truth, predicted, average='macro'))
