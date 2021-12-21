# tobias /run/media/ivan/Shared/BACKUP2/rUNSWift/runswift_cut_tobias.mlt
import os
import xml.etree.ElementTree as ET
import sys
import glob
import json


class MLTProducer:

    def __init__(self, id, filename, offset) -> None:
        self.id = id
        self.filename = filename
        self.offset = offset


class MLTEntry:

    def __init__(self, in_start, out_end, producer) -> None:
        self.in_start = in_start
        self.out_end = out_end
        self.producer = producer


class MLT:

    def __init__(self) -> None:
        self.producers = []
        self.entries


def read_producers(tree):
    root = tree.getroot()
    producers = []
    for x in root.iter("producer"):
        id = x.attrib["id"]
        if id.find("video") != -1:
            offset = int(x.attrib["offset"])
            filename = next(x.iter("property")).text.split("/")[-1]

            producers.append(MLTProducer(id, filename, offset))

    return producers


def read_entries(tree):
    root = tree.getroot()
    producers = read_producers(tree)

    prod_map = {}
    for prod in producers:
        prod_map[prod.id] = prod

    entries = []
    for x in root.iter("playlist"):
        if x.attrib["hide"] == "audio":
            for entry in x.iter("entry"):
                prod = prod_map[entry.attrib["producer"]]
                in_start = int(entry.attrib["in"]) + prod.offset
                out_start = int(entry.attrib["out"]) + prod.offset
                entries.append(
                    MLTEntry(in_start, out_start, prod))

    return entries


def in_halftime_pause(pos, firstEnd, secondStart):
    return pos >= firstEnd and pos <= secondStart


def in_set_state(pos, setStates):
    for setState in setStates:
        if pos >= setState["start"] and pos <= setState["end"]:
            return True

    return False


def clean_entries(entries, halftime, setStates):
    entries = sorted(entries, key=lambda entry: entry.in_start)

    last_entry = entries[0]
    result = []
    for entry in entries:
        if entry != last_entry:
            dist = entry.in_start - last_entry.out_end
            is_in_set_state = in_set_state(last_entry.out_end, setStates)
            is_in_halftime_pause = in_halftime_pause(
                last_entry.out_end, halftime["firstEnd"], halftime["secondStart"])

            if dist > 1:
                if not is_in_halftime_pause and not is_in_set_state:
                    last_entry.out_end = entry.in_start - 1

            if last_entry.producer.id == entry.producer.id:
                if not is_in_halftime_pause and not is_in_set_state:
                    result.remove(last_entry)
                    entry.in_start = last_entry.in_start

        result.append(entry)
        last_entry = entry

    return result


def write_to_csv(csv_file, entries):
    with open(csv_file, "w") as csv:
        print(f"Write to: {csv_file}")
        csv.write("video_id,offset,start,end")
        for entry in entries:
            csv.write("\n")
            csv.write(
                f"{entry.producer.filename},{entry.producer.offset},{entry.in_start},{entry.out_end}")


mlt_file = "/run/media/ivan/Shared/BACKUP2/rUNSWift"

with open('../game_config.json', 'r') as myfile:
    game_config = json.loads(myfile.read())

if len(sys.argv) == 3:
    out_file = sys.argv[2]
else:
    out_file = "out.csv"


if os.path.isdir(mlt_file):
    out_files = glob.glob(os.path.join(mlt_file, "*.mlt"))

    for mlt_file in out_files:
        if mlt_file.find("AutoSave") != -1:
            continue
        print(f"Processing {mlt_file}")

        tree = ET.parse(mlt_file)
        entries = read_entries(tree)
        cleaned_entries = clean_entries(
            entries, game_config["halftime"], game_config["setStates"])

        write_to_csv(os.path.join("cuts", mlt_file.split(
            "/")[-1].replace("mlt", "csv")), cleaned_entries)
        print()
