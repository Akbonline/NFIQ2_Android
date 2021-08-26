#include <jni.h>
#include <string>
#include <nfiq2api.h>
#include <nfiq2.hpp>
#include <vector>
#include<iostream>
#include <memory>
#include <cstring>
#include <android/log.h>

// for native asset manager
#include <sys/types.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <android/log.h>

#define  LOG_TAG    "native-lib"
#define  ALOG(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

using namespace std;

int mHeight;
int mWidth;
int mSize;

static uint8_t *mData;
string magicNumber;

std::string printUsage()
{
    return "NATIVE-LIB: Cannot find the file in the provided path!";
}


extern "C" JNIEXPORT jstring JNICALL
Java_com_example_aug12_12_MainActivity_getNfiq2Score(
        JNIEnv* env,
        jobject /* this */) {
            std::string hello = "Hello from C++";
            NFIQ2::Algorithm a;
            NFIQ2::ModelInfo modelInfoObj {};
            const std::string helpStr = { "-h"};

            try {
                /*
                 * Manually inputting the file location here. The path points to nist_plain_tir-ink.txt. This file is initially stored inside res -> raw folder.
                 * Then it's transferred to /data/user/0/com.example.aug12_2/files/nist_plain_tir-ink.txt folder.
                 * Similarly, inside the nist_plain_tir-ink.txt file, you need to provide the path to the yaml file and it is going to be in the same folder as the txt model file.
                 * But you'll have to manually provide the path to the YAML file inside the text file.
                 * */
                    modelInfoObj = NFIQ2::ModelInfo("/data/user/0/com.example.aug12_2/files/nist_plain_tir-ink.txt");

                    } catch (...) {

                        hello = "Could not parse model info file. "
                                   "Ensure it is the first argument on the cmd line\n";
                        hello = printUsage();
                        return env->NewStringUTF(hello.c_str());
            }

            NFIQ2::Algorithm model {};
            try {
                model = NFIQ2::Algorithm(modelInfoObj);
            } catch (...) {
                hello = "Could not initialize model from model info file\n";
                return env->NewStringUTF(hello.c_str());
            }

            uint32_t rows = mWidth;
            uint32_t cols = mHeight;
            std::shared_ptr<uint8_t> data {};

            NFIQ2::FingerprintImageData rawImage = NFIQ2::FingerprintImageData(                             // Creating a FingerprintImageData Object  <-> rawImage
                    mData, cols * rows, cols, rows, 0, 500);                          // Put the value of PPI

            // Calculate all feature values.
            std::vector<std::shared_ptr<NFIQ2::QualityFeatures::Module>> modules {};                        // Vector of (pointers of (QualityFeatures Module))
            ALOG("Native-lib size %d:", rawImage.width * rawImage.height);
            ALOG("Native-lib: PPI value inside NFIQ2 %d.", rawImage.ppi);

            ALOG("size of MData NFIQ2 %d.", rows*cols);

            try {
                modules = NFIQ2::QualityFeatures::computeQualityModules(                                    // Create computeQualityModules using the rawImage
                        rawImage);
            } catch (const NFIQ2::Exception &e) {
                hello = std::string("Error in calculating quality features: ")+ e.what()+'\n';           // If error in computeQualityModules
                return env->NewStringUTF(hello.c_str());
            }
            ALOG("Native-Lib: Value inside modules %d.", modules.size());

            // Pass the feature values through the random forest to obtain  an
            // NFIQ 2 quality score

            unsigned int nfiq2 {};
            try {
                nfiq2 = model.computeQualityScore(modules);
                ALOG("Native-Lib: NFIQ2 Score %d.", nfiq2);

            } catch (...) {
                ALOG("Native-Lib: Issues calculating the score");
            }
            // Actionable Feedback
            std::vector<std::string> actionableIDs =
                    NFIQ2::QualityFeatures::getActionableQualityFeedbackIDs();

            std::unordered_map<std::string, double> actionableQuality =
                    NFIQ2::QualityFeatures::getActionableQualityFeedback(modules);
            ALOG("--------------------------------------Native-Lib: Producing the detailed Actionable Feedback for the score-------------------------------------");
            for (const auto &actionableID : actionableIDs) {

                std::cout << actionableID << ": "
                          << actionableQuality.at(actionableID) << '\n';
                string actionableIdToString = actionableID;
                string actionableQualityToString = to_string(actionableQuality.at(actionableID));
                ALOG("Native-Lib: Actionable ID: %s",actionableIdToString.c_str());
                ALOG("Native-Lib: Actionable Quality value: %s",actionableQualityToString.c_str());

            ALOG("-----------------------------------Native-Lib: Producing the detailed Quality Feature Values for the score-------------------------------------");

            // Quality Feature Values
            std::vector<std::string> featureIDs =
                    NFIQ2::QualityFeatures::getQualityFeatureIDs();

            std::unordered_map<std::string, double> qualityFeatures =
                    NFIQ2::QualityFeatures::getQualityFeatureValues(modules);

            for (const auto &featureID : featureIDs) {
                string featureIdToString = featureID;
                string qualityFeaturesToString = to_string(qualityFeatures.at(featureID));
                ALOG("Native-Lib: Actionable ID: %s",featureIdToString.c_str());
                ALOG("Native-Lib: Actionable Quality value: %s",qualityFeaturesToString.c_str());

            }
            string NFIQ2Score = to_string(nfiq2);
            return env->NewStringUTF(NFIQ2Score.c_str());
}
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_aug12_12_MainActivity_setImageConstraints(JNIEnv *env, jobject thiz, jbyteArray imageData,
                                                           jint height, jint width) {
    // TODO: implement setImageConstraints()
    ALOG("Enter setImageConstraints %d.", __LINE__);
    jsize imageSize = env->GetArrayLength(imageData);
    ALOG("received image size  %d.", imageSize);

    mData = new uint8_t[imageSize];
    ALOG("created array %d.", mData[0]);

    env->GetByteArrayRegion(imageData,0, imageSize, reinterpret_cast<jbyte *> (mData));
    ALOG("created array %d.", mData[0]);

    mHeight = (int) height;
    ALOG("Height size %d.", mHeight);

    mWidth = (int) width;
    ALOG("Width size %d.", mWidth);

    return mData[0];
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_aug12_12_MainActivity_readAssets(JNIEnv *env, jobject thiz,
                                                  jobject asset_manager) {
    // TODO: implement readAssets()
    AAssetManager* mgr = AAssetManager_fromJava(env, asset_manager);

    assert(NULL != mgr);
    AAsset* testAsset = AAssetManager_open(mgr, "example.txt", AASSET_MODE_UNKNOWN);
    if (testAsset)
    {
        assert(testAsset);

        size_t assetLength = AAsset_getLength(testAsset);

        __android_log_print(ANDROID_LOG_DEBUG, "Native Audio", "Asset file size: %d\n", assetLength);

        char* buffer = (char*) malloc(assetLength + 1);
        AAsset_read(testAsset, buffer, assetLength);
        buffer[assetLength] = 0;

        __android_log_print(ANDROID_LOG_INFO, "Test Asset Manager", "The value is %s", buffer);

        AAsset_close(testAsset);
        free(buffer);
        string res = (string)buffer;
        return env->NewStringUTF(res.c_str());
    }
    else
    {
        __android_log_print(ANDROID_LOG_ERROR, "Test Asset Manager", "Cannot open file");
    }


}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_aug12_12_MainActivity_passResourcesToJNI(JNIEnv *env, jclass clazz,
                                                        jstring yourpath) {


    __android_log_print(ANDROID_LOG_INFO, "Set Configuration:", "Entering the function");

    const char *nativeString = env->GetStringUTFChars(yourpath, 0);
    __android_log_print(ANDROID_LOG_INFO, "Set Configuration:", "Path value =",yourpath);

    ifstream myfile (nativeString);                                             // Files are stored inside the /data/user/0/com.example.aug12_2/files/ directory

    string line;
    string output;


    if (myfile.is_open())
    {
        __android_log_print(ANDROID_LOG_INFO, "Set Configuration:", "Able to open the file!");
        while ( getline (myfile,line) )
        {
            output+=line;
        }
        myfile.close();
    }
    else{
        __android_log_print(ANDROID_LOG_INFO, "Set Configuration:", "Not able to open the file");
    }
    __android_log_print(ANDROID_LOG_INFO, "Set Configuration:", "Exiting the function");

    return env->NewStringUTF(output.c_str());
}