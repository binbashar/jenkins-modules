#!/usr/bin/env groovy
// NOTE: File names have '_' instead of '-' because of the issue detailed in the link below:
// https://stackoverflow.com/questions/36461121/groovy-calling-a-method-with-def-parameter-fails-with-illegal-class-name

// TEST-1
ArrayList listParamNames = ['paramName1', 'paramName2', 'paramName3', 'paramName4', 'paramName5','paramName6',
                            'paramName7', 'paramName8', 'paramName9', 'paramName10', 'paramName11']

ArrayList listParamNamesRef = [['paramName1', 'paramName2', 'paramName3', 'paramName4', 'paramName5','paramName6',
                            'paramName7', 'paramName8', 'paramName9', 'paramName10'], ['paramName11']]

ArrayList testResult1 = sliceList(listParamNames, 10 )

if (testResult1 == listParamNamesRef) {
    print "\nreturn value: ${testResult1} \n"
    print "TEST PASSED!!!\n"
} else {
    print "TEST FAILED\n"
}

// FUNCTION-1
static def sliceList (ArrayList list, int sliceSize) {
    def listOfSlices = []

    def slice = []
    int listSize = list.size()
    for (int i = 0; i < listSize; i++) {
        slice.add(list.get(i))

        if (((i + 1) % sliceSize) == 0) {
            listOfSlices.add(slice)
            slice = []
            continue
        }

        if (i == (listSize - 1)) {
            listOfSlices.add(slice)
        }
    }

    return listOfSlices
}