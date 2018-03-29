import hashlib

import config

class color:
   PURPLE = '\033[95m'
   CYAN = '\033[96m'
   DARKCYAN = '\033[36m'
   BLUE = '\033[94m'
   GREEN = '\033[92m'
   YELLOW = '\033[93m'
   RED = '\033[91m'
   BOLD = '\033[1m'
   UNDERLINE = '\033[4m'
   END = '\033[0m'


PATTERN_FLOOR = 1000000

patterns = {}
pattern_hashes = {}

class Pattern:

    def __init__(self, sequence):
        self.sequence = sequence.sequence
        self.tracePositions = []
        self.durations = []

    def addPosition(self, startTime, endTime):
        self.tracePositions.append(startTime)
        self.durations.append(endTime - startTime)

    # When comparing sequences, we skip negative numbers,
    # as they represent repetitions. We compare only
    # positive numbers. Also, with this comparison function,
    # if a shorter pattern is a prefix of a longer pattern,
    # the two patterns will be deemed the same.
    #
    # Furthermore, if we detect a pattern within a sequence, that is,
    # a number above the PATTERN_FLOOR, we deem two sequences the same
    # if they have different patterns in the same slot, but the same
    # functions otherwise. To keep track of the differences, we simply
    # merge sets of patterns encountered at the same index.
    #
    def sameNonRepeating(self, newSequence):

        commonSetPositions = []

        if len(self.sequence) != len(newSequence): return False

        for i in range(0, len(self.sequence)):
            # Find the next positive element of each sequence
            #
            old_curElement = self.sequence[i]
            new_curElement = newSequence.sequence[i]

            # If the two elements are sets, we keep track of where they
            # occur. If the two patterns are the same in every function
            # they include, but not in the patterns that they contain,
            # we still deem them as they same and we merge the elements of
            # the old set into the new set.
            #
            if (isinstance(old_curElement, set) and
                isinstance(new_curElement, set)):
                commonSetPositions.append(i)
            elif (old_curElement != new_curElement):
                # Compare them
                return False

        # We are about to report these two patterns as being the same.
        # However, as explained in the comment above, their enclosed
        # pattern numbers might not be. If they are not, we have to add
        # the set elements of the new pattern to the sets in the corresponding
        # positions of old pattern. That is because the new pattern will be
        # discarded by the calling code once we return True.
        #
        for i in commonSetPositions:
            self.sequence[i] |= newSequence.sequence[i]

        # If the existing pattern is shorter than the new pattern, we have to
        # append to it the elements of the new pattern that were not part
        # of the existing pattern. Otherwise, these elements will get lost.
        # #
        # if (len(newSequence.sequence) > len(self.sequence)):
        #     self.sequence.extend(
        #         newSequence.sequence[j:len(newSequence.sequence)])

        return True

    def hash(self): 
        m = hashlib.sha256()
        for element in self.sequence:
            update = ""
            if isinstance(element, set): update = "s"
            else: update = element
            m.update(str(update).encode())

        return m.digest()

    # Check if the new sequence is the same as the
    # sequence contained within the pattern.
    #
    def same(self, newSequence):

        if (len(newSequence.sequence) != len(self.sequence)):
            return False

        for i in range(len(newSequence.sequence)):
            if (newSequence.sequence[i] != self.sequence[i]):
                return False

        return True

    def printMe(self, file):

        file.write(str(self.sequence) + "\n")

        if (len(self.tracePositions) !=
            len(self.durations)):
            print(color.RED + color.BOLD + "Mismatch in lengths of " +
                  " trace positions and durations arrays.")

        length = len(self.tracePositions)
        if (len(self.durations) > length):
            length = len(self.durations)

        file.write(str(length) + " OCCURRENCES.\n")
        for i in range(length):
            file.write(str(self.tracePositions[i]) + " : "
                       + str(self.durations[i]) + "\n")

# A sequence is a list of functions and patterns. A sequence has a start time
# and an end time. A sequence is usually in-flux: functions are being added to
# it, and it is being periodically compressed.
#
# Once the sequence has ended, which happens when we go down a stack level,
# we turn the sequence into a pattern.
#
class Sequence:

    def __init__(self, time):
        self.sequence = []
        self.startTime = time
        self.endTime = 0

    def add(self, funcID, time):
        self.sequence.append(funcID)
        self.endTime = time

        self.compressVeryLossy()

    # This is a version of very lossy compression. Here we no longer
    # encode the repeating sequences. If we find a repeating sequence,
    # we just drop it. This is done to reduce the number of patterns,
    # reduce the length of sequences and improve the runtime.
    #
    def compressVeryLossy(self):

        lastFuncIdx = len(self.sequence) - 1
        lastFuncID = self.sequence[lastFuncIdx]

        # Search for the same function ID or for a set that includes
        # a pattern if lastFuncID is actually a set.
        for i in range(lastFuncIdx - 1, -1, -1):

            if ( (self.sequence[i] == lastFuncID) or
                 (isinstance(lastFuncID, set) and
                  isinstance(self.sequence[i], set))):

                candidateListLength = lastFuncIdx - i

                if ((i+1) - candidateListLength < 0):
                    return False

                # Continue looking for a suitable candidate.
                # May increase the running time.
                if not self.same(i + 1, lastFuncIdx + 1,
                                 i-candidateListLength+1, i + 1):
                    continue

                # The final part of the sequence is the same as the one
                # preceding it. So we just remove it. Before
                # deleting the final piece of the sequence, merge any
                # sets that we encounter.
                #
                # sublist1 contains the sequence we are about to delete.
                #

                sublist1 = self.sequence[(i+1):(lastFuncIdx + 1)]

                for idx in range(0, candidateListLength):
                    idx1 = len(self.sequence) -  candidateListLength*2 + idx

                    if (isinstance(self.sequence[idx1], set) and
                        isinstance(sublist1[idx], set)):
                        self.sequence[idx1] |= sublist1[idx]

                del self.sequence[(i+1):(lastFuncIdx + 1)]
                return True

        return False

    # Add to the dictionary of patterns
    def finalize(self):
        global patterns
        global pattern_hashes

        # The reason for having a pattern floor is because we want
        # to distinguish between functions and patterns, both of which
        # are encoded using numbers. By using a pattern floor, we
        # ensure that these numbers come from different name spaces.
        # Functions will be encoded as numbers smaller than PATTERN_FLOOR.
        # Patterns will be encoded as numbers equal to or larger than
        # PATTERN_FLOOR. PATTERN_FLOOR has to be large enough to encode
        # all unique function names.
        #
        global PATTERN_FLOOR

        hash = self.hash()
        if hash in pattern_hashes:
            patternID = pattern_hashes[hash]
            pattern = patterns[patternID]
            pattern.addPosition(self.startTime, self.endTime)
            return patternID

        newPattern = Pattern(self)
        newPattern.addPosition(self.startTime, self.endTime)
        patternID = len(patterns) + PATTERN_FLOOR
        patterns[patternID] = newPattern
        pattern_hashes[newPattern.hash()] = patternID
        return patternID

    def printMe(self):
        i = 0
        print("SEQ: start=" + str(self.startTime) + ", end="
              + str(self.endTime))
        print("["),
        for item in self.sequence:
            if (i > 0):
                print(", "),
            print(str(item)),
            i += 1

        print("]")

    def hash(self): 
        m = hashlib.sha256()
        for element in self.sequence:
            update = ""
            if isinstance(element, set): update = "s"
            else: update = element
            m.update(str(update).encode())

        return m.digest()

    # If two subsequences contain difference patterns we still deem
    # them identical, as long as they have the same non-pattern
    # sequences of numbers.
    #
    def same(self, int1_begin, int1_end, int2_begin, int2_end):

        if ( (int1_end - int1_begin) != (int2_end - int2_begin)):
            return False

        i = int1_begin
        j = int2_begin


        while (i < int1_end and j < int2_end):

            if ( ((isinstance(self.sequence[i], set)) and
                  (isinstance(self.sequence[j], set))) or
                 (self.sequence[i] == self.sequence[j])):
                i += 1
                j += 1
            else:
                return False

        return True

currentStackLevel = 0
currentSequence = None
sequenceForLevel = {}

def minePatterns(funcName, stackLevel, startTime, endTime):

    global currentStackLevel
    global currentSequence
    global sequenceForLevel

    funcID = funcNameToID(funcName)

    # We are going up a stack level. Let's stash the current pattern.
    # We will use it later once we are back at this stack level.
    #
    if (stackLevel > currentStackLevel):

        # Stash the current sequence
        if (currentSequence is not None):
            sequenceForLevel[currentStackLevel] = currentSequence

        currentSequence = Sequence(startTime)
        currentStackLevel = stackLevel

    # We have encountered a completed function at the same level as
    # we were previously. Let's see if we can compress the remembered
    # pattern for the current level on the fly.
    #
    if (stackLevel == currentStackLevel):
        if (currentSequence is None):
            currentSequence = Sequence(startTime)
        currentSequence.add(funcID, endTime)

    # We are going down the stack level. This means that the parent of
    # the completed functions we have just processed has completed.
    # Let's record the pattern we derived and retrieve the pattern for
    # the new stack level.
    #
    if (stackLevel < currentStackLevel):
        if (currentStackLevel - stackLevel > 1):
            print(color.BOLD + color.RED +
                  "Warning: jumping down more than one stack level.")
            print(funcName + ": stack level is " + str(stackLevel) +
                  ", previous stack level was " + str(currentStackLevel))
            print(color.END)
            return

        childPatternID = currentSequence.finalize()
        currentStackLevel = stackLevel

        if (currentStackLevel in sequenceForLevel):
            currentSequence = sequenceForLevel[currentStackLevel]
            if (currentSequence is None):
                print(color.BOLD + color.RED)
                print("Warning: retrieved a null current sequence")
                print(color.END)
        else:
            currentSequence = Sequence(startTime)

        currentSequence.add(funcID, endTime)
        newSet = set()
        newSet.add(childPatternID)
        currentSequence.add(newSet, endTime)

# We reached the end of the trace. We need to finalize the current sequence.
#
def finalizePatterns(endTime, prefix):

    global currentSequence

    currentSequence.endTime = endTime
    currentSequence.finalize()


def countNonNegativeNumbers(list):

    count = 0

    for number in list:
        if (number >= 0):
            count += 1
    return count

funcToID = {}
idToFunc = None


def funcNameToID(funcName):

    global funcToID

    if (not funcName in funcToID):
        funcToID[funcName] = len(funcToID)

    return funcToID[funcName]


def mine_thread(file):
    with open(file, "r") as f:
        stack = []
        start_time = []

        count = 0
        for line in f:
            if (count % 100000 == 0): print(count)
            count += 1

            record = line.split("\n")[0].split("\t")

            func_id = record[0]
            dir = record[1]
            time = record[2]

            if (dir == "0"):
                stack.append(func_id)
                start_time.append(time)
            else:
                if (stack[len(stack) - 1] != func_id): 
                    print("error in data!")

                func = stack.pop()
                start = start_time.pop()
                end = time

                minePatterns(func, len(stack), int(start), int(end))



def mine_all_treads():
    file_name = "data/threads/thread_trace_19.txt"
    mine_thread(file_name)

    global patterns

    with open(config.PATTERNS_DIRECTORY + "thread_19_patterns.txt", "w") as pattern_file:
        for pattern_id in patterns:
            pattern = patterns[pattern_id]
            pattern_file.write("#############\n")
            pattern_file.write(str(pattern_id) + ":\n")
            pattern.printMe(pattern_file)
            pattern_file.write("\n")



def split_threads():
    with open(config.TRACE_FILE, "r") as trace_file:
        line = trace_file.readline()
        while (line[0:4] != "COPY"): 
            line = trace_file.readline()
            continue

        thread_files = {}

        functions_map = {}
        functions_list = []

        line = trace_file.readline().split('\n')[0]

        count = 0
        while line != "":
            if (count % 1000000 == 0): print(count)
            count += 1

            record = line.split('\t')
            
            dir = record[1]
            func = record[2].split('\"')[1]
            tid = record[3]
            start = record[4]
            duration = record[5]

            if not "::" in func:
                if not func in functions_map:
                    functions_map[func] = str(len(functions_list))
                    functions_list.append(func)
                
                func_id = functions_map[func]
                new_record = [func_id, dir, start, duration]

                if not tid in thread_files:
                    new_thread_file_name = config.THREAD_TRACE_DIRECTORY + "thread_" + tid + "_trace.txt"
                    thread_files[tid] = open(new_thread_file_name, "w")

                cur_thread_file = thread_files[tid]
                cur_thread_file.write("\t".join(new_record) + "\n")

            line = trace_file.readline().split('\n')[0]
        

        for tid, file in thread_files.items():
            file.close()

        with open(config.FUNCTION_FILE, "w") as function_file:
            for function in functions_list:
                function_file.write(function + "\n")


def main():
    mine_all_treads()

if __name__ == "__main__":
    main()