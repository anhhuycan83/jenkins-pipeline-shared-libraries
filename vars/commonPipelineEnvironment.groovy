import java.util.concurrent.*
import java.util.concurrent.atomic.*
import org.json.*;
import jenkins.model.Jenkins


class commonPipelineEnvironment implements Serializable {
    def jenkins_env = Jenkins.instance.getGlobalNodeProperties()[0].getEnvVars()        // In Jenkins Configure -> (Disable deferred wipeout on this node) should be unchecked for this value to be fetched
    boolean isDistributedJenkinsPipeline = false
    def releasesInCentralisedJenkins = "22.9-release,22.12-release,23.3-release,23.6-release,23.9-release,23.12-release,24.3-release,24.6-release,23.12.6-1-release,24.9-release,23.6.8-1-release,24.12-release"
    def multibranchPipeline = "Altiplano-NextGenDistributed-featureBranches"
    long pipelineStartTime = 0;
    def uuid = null;
    boolean isComponentCoveragePassed = false
    Semaphore semaphoreForATReport
    def sonarFailedComponentMap = [:]
    def msToTrigger = [];
    def changedRepos = [];
    def changedRepoAuthors = [];
    def changedTopicRepoAuthors = [];
    def changedRepoTicketID = [];
    def changedCommitID = [];
    def completedSonar = [];
    def oldSonarData = [:];
    def flakyTestTags = [];
    def flakyUt = false;
    def failedTestCasesInLastRetry = new HashSet([])
    def fossPipelineList = null;
    def fossRetry = 3;
    boolean isPassed = true;
    boolean isK8SSlave = false;
    def clusterLBIP;
    def clusterNodeIP;
    def pushJacocoResult = false;
    def configuration;
    def commonLib;
    def http_api_server = jenkins_env.ENV_TYPE == 'QA' ? "http://10.183.209.235" : "http://10.183.211.12"
    def http_api_server_qa = "http://10.183.209.235"
    def buildRepoConfig=[:];
    def repoList;
    def pipelineDetailsFromDB
    def reposToCreateMaster = null;
    def dockerInfo;
    def branch;
    def allocatedNode;
    def cloudSettings = null;
    def cloudName = "OHN001"
    def cloudMapping = ['OHN001':'Finland',
                        'OHN002':'Finland',
                        'DHN40':'Bangalore',
                        'DHN55':'Finland',
                        'DHN86':'Finland',
                        'OHN26':'Finland',
                        'OHN81':'Finland',
                        'CHENNAI':'Chennai',
                        'DHN88':'Bangalore',
                        'OHN70':'Finland',
                        'OHN58':'Finland',
			'RHN01':'Chennai',
                        'DHN78':'Finland']
    def skipBBAMake = false;
    def runDeployCRStage = false;
    def runReleaseStage = false;
    def isFeatureBranch = true;
    def runAsSlaveReg = true;
    def deployToArtifactory = false;
    def enableATDebugLog = false;
    def eeCommitInSetup = ""
    def enableATExcludeTag = null;
    def forceCleanup = true;
    def isPushingDataIntoInfluxdb = true
    def hasDataToday = false
    def discardMail = true;
    def build_tag = '1.0.0-SNAPSHOT';
    def helm_tag = null
    def bbaBuildTag = null
    def pipelineDependencyLastSuccessfulBuild
    def releaseType = 'MAIN';
    def sonarProjectName = 'sonarProject';
    def promoteFB = false;
    int buildNumber;
    def devSpecificConfig;
    def pipelineSpecificConfig;
    def nonCodedPlugsFile;
    def nonCodedPlugContent;
    def isFNMSImagePipeline = false;
    def isReleaseBranch;
    def FNMSImage_Version = null;
    def eonuVersion = ''
    def artMaven;
    def artDocker;
    def artDockerInternal;
    def artHelm;
    def artInternal;
    def artDescriptor;
    def artThirdPartyDocker;
    def buildInfoNumber;
    def buildInfoDocker;
    def buildInfoDockerInternal;
    def buildInfoMaven;
    def buildInfoHelm;
    def buildInfoInternal;
    def buildInfoDescriptor;
    def buildInfoThirdPartyDocker;
    def artifactoryServer;
    def dockerImagesToDeployConfig = null;
    def dockerImagesToDeployInternalConfig = null;
    def platformDockerImagesToDeploy ;
    def mavenArtifactsToDeployConfig = null;
    def internalDependenciesToDeployConfig = null;
    def helmArtifactsToDeployConfig = null;
    def nonCodedPlugToDeployConfig = null;
    def artifactorySpecificConfig;
    def utAppComponents;
    def utPlugComponents;
    def sonarComponents;
    def utOnlyTestComponents;
    def haveRerunStage = false;
    def useUpstreamBuildTag;
    def rfLintToRun = null;
    def port = 9000;
    def httpsScmSlaveURL = ['Bangalore':'gerrit-bhsl2.int.net.nokia.com',
                            'Chennai':'gerrit-bhsl2.int.net.nokia.com',
                            'Finland':'gerrit-essl5.int.net.nokia.com',
                            'built-in':'gerrit-essl5.int.net.nokia.com']
    def scmSlaveURL = ['Bangalore':'gerrit-bhsl2.int.net.nokia.com:29418',
                       'Chennai':'gerrit-bhsl2.int.net.nokia.com:29418',
                       'Finland':'gerrit-essl5.int.net.nokia.com:29418',
                       'built-in':'gerrit-essl5.int.net.nokia.com:29418']
    def scmMasterUrl = 'gerrit.ext.net.nokia.com'
    def portToPullFrom = 9000;
    //default settings for checkout common lib
    def commonLibSettings = ['scmType':'GitSCM',
                             'scmURL':'gerrit-essl5.int.net.nokia.com:29418',
                             'httpsScmURL':'gerrit-essl5.int.net.nokia.com',
                             'projectName':'FNMS',
                             'repoName':'jenkin-common-lib',
                             'relativeTargetDir':'jenkin-common-lib',
                             'userName':'ca_fnmst001_2',
                             'credentialsId':'jenkins']

    def branchSpecificFileSettings = ['scmType':'GitSCM',
                                      'scmURL':'gerrit-essl5.int.net.nokia.com:29418',
                                      'httpsScmURL':'gerrit-essl5.int.net.nokia.com',
                                      'projectName':'FNMS',
                                      'repoName':'ee-environment',
                                      'relativeTargetDir':'env',
                                      'relativetargetDirPreBuild':'envPreBuild',
                                      'userName':'ca_fnmst001_2',
                                      'credentialsId':'jenkins']

    def cbtATSettings = ['scmType':'GitSCM',
                         'scmURL':'gerrit-essl5.int.net.nokia.com:29418',
                         'httpsScmURL':'gerrit-essl5.int.net.nokia.com',
                         'projectName':'FNMSEE',
                         'repoName':'cbt-at',
                         'relativeTargetDir':'cbt-at',
                         'userName':'ca_fnmst001_2',
                         'credentialsId':'jenkins'
                         ]
    def DeploymentRepoForFossSetting = ['scmType':'GitSCM',
                                        'scmURL':'gerrit-essl5.int.net.nokia.com:29418',
                                        'httpsScmURL':'gerrit-essl5.int.net.nokia.com',
                                        'projectName':'FNMS',
                                        'repoName':'deployment',
                                        'relativeTargetDir':'deployment',
                                        'userName':'ca_fnmst001_2',
                                        'credentialsId':'jenkins'
                                    ]
    JSONArray flakyTestCaseJsonArray = new JSONArray();
    def flakyTestCaseJsonMap = [:]
    JSONArray flakyUtJsonArray = new JSONArray();
    def flakyUtJsonMap = [:]
    JSONArray passTestCaseJsonArray = new JSONArray();
    def fbInfluxDetails = [:]
    def passTestCaseJsonMap = [:]
    def pipelineDependencyMatrix = [:]
    def msPipelineDependencyMatrix = [:]
    def downstreamTriggerCount = 0
    def pipelineToTestMapping = [:]
    def appsPipelineMatrixDependency = [:]
    def individualHelmRepoList = []
    def sstPipelineList = []
    def helmchartBaseUrl = [:]
    def pipelineHelmNaming = [:]
    def failureMessage = ""
    //default settings for config file
    def globalFilePath = '/jenkin-common-lib/resources/globalConfiguration.yml'
    def excludeIntentsFilePath = '/jenkin-common-lib/resources/excludeIntentsAPI.yml'
    def configFilePath = '/env/boxes/common-box/ConfigFile_Altiplano.groovy';
    def integrationConfigFilePath = '/env/boxes/common-box/integrationConfiguration.groovy';
    def runOnlyValidation = false;
    def automationRepos = ["ACAutomation","ANVAutomation","FCAutomation"]
    def runSharedMode = false;
    def staticSuiteSelection = false
    def helm_Tags = []
    def build_Tags = []
    def helm_Version = []
    def repoToBuild = []
    def repoToDeploy = []
    def automationFilePath = null;
    def pipelineSpecificConfigFilePath = '';
    def automationSettings
    def automationMapping
    def familyTypePlug;
    def plugVariantPlug;
    def interfaceVersionPlug;
    def cleanupScriptPath = '/env/cleanupDiskOnJenkinsSlave.sh';
    def workspacePath = '/home/jenkins/workspace';
    def altiplanoBuildTag;
    def fwaBuildTag;
    def nonrefBuildTag;
    def altiplanoBuildNumber;
    def deployExtraArtifacts;
    def altiplanoNetConfBuildTag;
    def altiplanoNetConfBuildNumber;
    def altiplanoBPBuildTag;
    def altiplanoBPBuildNumber;
    def nokiaUrl = null;
    def nokiaChannel = null;
    def nokiaLogin = null;
    def nokiaPwd = null;
    def atTypes = ['REF','ENS','EXT','NONREF'];
//   common
    def reRunTags = new ConcurrentHashMap<String,ArrayList<Object>>()
    def failedSuites = new ConcurrentHashMap<String,String>()
    def failedSuitesToPrefix = new ConcurrentLinkedQueue();
    def suitesStatus =  new ConcurrentLinkedQueue();

//   python
//    def reRunTagsPython = new ConcurrentHashMap<String,ArrayList<Object>>()
    def setupTagFailPython=[:] // check AtomicBoolean.
    def failTestCountPython = new AtomicInteger(0);
    def totalTestCountPython = new AtomicInteger(0);
    def failTestCountRerunPython = new AtomicInteger(0);
    def totalTestCountRerunPython = new AtomicInteger(0);
//    def failedSuitesPython = new ConcurrentHashMap<String,String>()
//    def failedSuitesToPrefixPython = new ConcurrentLinkedQueue(); // [tag, tagPrefix + tag, repoName, <sharedSetup or sharedSubTag>, sharedSubTagName, sharedSubTagRepo, automationRepo]
//    def suitesStatus =  new ConcurrentLinkedQueue();
    def isPullFailedPython = new AtomicBoolean(false);
//  normal
//    def reRunTags=[:];
    def setupTagFail
    def failTestCount;
    def totalTestCount;
    def failTestCountRerun=null
    def totalTestCountRerun=null
    def flakyTestCaseList = [];
    def flakyTestCaseListWithTag = [];
    def fbSuiteFailure = false
//    def failedSuites = [:];
//    def failedSuitesToPrefix = []; // [tag, tagPrefix + tag, repoName, <sharedSetup or sharedSubTag>, sharedSubTagName, sharedSubTagRepo, automationRepo]
    def fbSuiteReRunnable = [] as Set
//    def suitesStatus = [];
    def ATResultList = []
    def isPullFailed = false;
    def runSonar = true;
    def runFoss = true;
    def runAnchore = true;
    def publishDescriptorToDV = false;
    def publishMsDescriptorToDV = false;
    def anchoreBailOnFail = true;
    def anchoreBailOnPluginFail = true;
    def getImagesForFossFromHelm = false;
    def runOnlyDockerFoss = false;
    def failForNewDockerFoss = false;
    def runFossForComponentPipelines = false;
    def dependencyInfoForFoss = [:]
    def runRFLint = true;
    def atCommonExtraSettings = ''
    def pipelineName = null;
    def fnmsVersions = [:];
    def thirdPartImgVersions =[:];
    def nonDeliverableImgVersions=[:];
    def domainSubDomainMapping=[:];
    def failedThirdPartyImages=[:];
    def failedFNMSImages=[:];
    def failedNonDeliverableImages=[];
    def newDockerFossFound=[];
    Boolean automationRun=false;
    Boolean isDisablePipelineOnconsecutiveFailure;
    Boolean pushDataToInflux;
    def falkyUtRepoList = [];
    def directDeployCR = false;
    def releaseBuildNumber;
    def isReleaseBuild = false;
    def isBBA = false;
    def splitBuildInfoMaven = false;
    def isSolution = false;
    def useLastIntegrationHelm = false;
    def useSuccessNonRefAltiplanoBuild = false;
    def useSuccessSDANInt = false;
    def allCommits
    def allCommitsForTag
    def pipelineDependency = [:]
    def nonCodedPlugList=[:];
    def factorySwPlugList=[:];
    def factorySwPlugListFromParent=[:];
    def nonCodedPlugListFromParent=[:];
    def superFB=false;
    Date startedTime;
    def domainRegressionPipelines=[];
    def pipelineList = [];
    def reRunBasedOnPercentage=false;
    float reRunTCThresholdPercentage = 5
    int reRunTCThresholdCount = 60
    int reRunSuiteThresholdCount = 6
    int failureThresholdPercentage = 2
    def disableRerun = false
    def cbtLib
    def analyzeClassFiles = false
    def analyzeExecFiles = false
    def archiveClassFiles = false
    def jobNameSuffix = ''
    def sqlstr = ""
    def msDescriptorPath = []
    Map nonCodedPlugListFromInput = [:]
    Map nonCodedPlugDownloadPath = [:]
    Map nonCodedPlugHostSimDownloadPath= [:]
    Map atTagToRunForDeviceSoftware = [:]
    Map impactedNonCodedPlugs = [:]
    def deviceExtensionUrlConstant = "/vobs/dsl/sw/y/build/altiplano/generated/"
    def deviceExtensionHostSimUrlConstant = "/vobs/esam/build/host/moswa/images/"
    def nonCodedDefaultSettings = [:];
    Map devExtnMap = [:]
    Map devExtnFactorySwMap = [:]
    def pipelinetoRun = [];
    def pipelinesToRun = null;
    def artifactoryRepo = "";
    def deviceExtnBuild = "";
    def isClickTest = false;
    def skipAutomationFailure = false
    def returnResultPullme = false
    def lightSpanJenkinsDetails=[:]
    def parentDescriptorCommitDetails = [:]
    def parentDescriptorTagDetails = [:]
    def parentCBTDetails = [:]
    def microServiceClassPathUrlsForCBT = ""
    def FBCBTBasePipeline = null
    def FBCBTBasePipelineBuild = null
    def internalParentDescriptorTagDetails = [:]
    def buildMatrixDependency = [:]
    def deployMatrixDependency = [:]
    def parentPipelineTag = [:]
    def testPipelineTag = [:]
    def parentBuildMatrix=[:]
    def parentBuilds =[:]
    def microServiceNames = [:]
    def pipelineNameMapping = [:]
    def parentDockerImages = [:]
    def parentDockerInternalImages = [:]
    def reposBuilt=[];
    def repoBuildFailed=[];
    def reposDeploy=[];
    def folderRepoList=[];
    def tagDetails=[:];
    def skipRunFailedTCs=[:]
    def setupSharedTagFailOnValidation = false
    int defaultSaveDays = 4;
    def buildChildArray = [];
    def cbtImpactedTest=[:];
    def cbt = false
    def cbtPipelinesToIgnore = [];
    def pullHelmForFossFromArtifactory = false;
    def dailyBuildPipeline = "Altiplano-LightSpan-Extension-Sanity";
    def generateCRNDoc = false;
    def distributedtcs=[:];
    def reBuild
    def osVersion = "20.04"
    def k8sVersion = "1.26.6"
    def containerRuntime = "containerd"
    def vmPool
    def cbtBuildNumber = null;
    boolean devtoolsCheckout = false;
    boolean buildmeOnly = false;
    def nextGenFB = false
    def atFB = false
    def finalPipelineToRun = []
    def k8sSetup = 'minikube';
    def npmRegURL = 'http://localhost:4873'
    def pipelineBuildStatus = [];
    def cbtImpactedSuitesCount = 0;
    def topicDetailList = [];
    def stagingPipeline = false
    def cmdAbandon = [];
    def cmdApprove = [];
    def cmdRebase = [];
    def cmdRestore = [];
    def cmdSubmit = [];
    def cmdGerrit = [];
    def skipAutomationRepo = [];
    def cbtImpctedTc = 0;
    def cbtNewTestCase = 0;
    def fbkpi = [
     'pipeline': '',
     'bugId' : '',
     'buildNumber' : '',
     'triggeredBy' : '',
     'pipelinesToRun' : [],
     'cbt' : [
         'enabled' : false,
         'pipeline' : []
     ],
     'domainRegression' : [
         'enabled' : false,
         'pipeline' : []
     ],
     'rf_critical_total' : 0,
     'rf_critical_passed' : 0,
     'rf_critical_failed' : 0,
     'date' : '',
     'result' : '',
     'failureStage' : ''
    ]
    def influxFBPublishedTags = []
    def validationStepStatus = [
        'validation_step_suiteFailure' : 'NA',
	'validation_step_suitFailureCount' : '0'
    ]
    def influx_validation_rerun_steps = [
       'fb_rerun_type' : 'NOT_RUN', // 'integration' -> FB in integration rerun mode, 'domainRegression' -> suite selected rerun + Flaky matching 
       'fb_validation_integ_status' : 'NOT_RUN', // ['NOT_RUN'(not going to integration rerun mode), 'GOING_TO_RERUN' (going to integration rerun), 'THRESHOLD_ERROR' (failed to rerun as it didn't match threshold criteria)]
       'fb_validation_suite_rerun_status' : 'NOT_RUN', // ['NOT_RUN' (suite rerun condition not entered, i.e no suite failed or integration rerun), 'PROCESSING_ERROR' (script/internal error), 'NO_MATCH_OR_THRESHOLD_ERROR' (failed suites, not matching rerun suite condition or matched suites > thereshold), 'MATCH_FOUND' (found suites to rerun after match)]
       'fb_validation_flaky_tc_status' : 'NOT_RUN' // ['NOT_RUN' (flaky condition check not entered, i.e no testcase failed or integration rerun), 'PROCESSING_ERROR' (script/internal error), 'NO_MATCH_OR_THRESHOLD_ERROR' (failed tags, not matching flaky condition from flakyMasterList), 'MATCH_FOUND' (found suites to rerun after match)]
    ]
    def runAutomationInDockerContainer = false;
    def isIndependentMicroservice;
    def isNSA = false
    def clicktestArtifactsToDeploy = [];
    def deployedArtifactsForClicktest = "";
    def iaDetailsForFB = "NA";
    def descriptorObj = null
    def isPlatform = false;
    def noPlatformChange = [];
    def platformChange = [];
    def platformBuild = [];
    def prevPlatformData = null
    //def cliclTestFBArtifactLoccation = "artifactory"
    def ignoreParentData = [:]
    def queueTimeForExecutor = 0
    def pipelineSetupTime = 0
    def integrationConfigReader = null
    def ignorePublishPromoteMsForIntegration = [:]
    def syncDateThreshold = 2
    def isFlakyinFB = false
    def isFlakyUtinFB = false
    def isCandidateForMerge = false
    def runAppsPipelineMatrixDependency = false
    def baselineType = "";
    def integrationType = "core"
    def wayToMerge
    def mergedFrom
    def isMergeRun = false
    def candidateBuild = ''
    def SQLCoreTableName;
    def isExtension = false
    def generateProductInfo = false
    def CBT_pipelinesToRunString = null
	def cbtBaselinePipelineName = null
	def lsBranch = null
    def runSSTOnly
	def isIntOnlyFB = false
    def drSelectedInFB = false
    def passTagToTestCasesMap = [:]
    def flakyTagToTestCasesMap = [:]
//    def passTestCaseMap = [:]
//    def flakyTestCaseMap = [:]
    def triggerDR = false // will be set to true only in validation stage
    def changedFileType = []
    def automationChangedFiles = [:]
    def upStreamBuilds
    def changesToRepo
    def testSuiteTotalMappingActual = [:]
    def testSuiteTotalMappingCBTAT = [:]
    def cbtAtJson
    def totalATCount = 0
    def runPythonCAF = true
    def platformRepos = ["netconf-lib", "base-platform", "java-base-nano","controller-platform", "pm", "package"]
    def buildToAvoidPy = ['ANVAutomation','ACAutomation','FCAutomation','AppsAutomation']
    def avoidBuildConfig = [:]
    def checkedOutRepos = []
    def updateBuildStatusInQueueServer = false
    def checkoutFailedRepos = []
    def skipHealthCheckOnAT = []
    def createWheelRepos = []
    def cbtAT
    def impactedtcs = [:]
    def cbtSnapshotPath = [:]
    def runAutomationContainer = true
    def buildCommitDetails = [:]
    def repoTriggerinFB
    def semVer = ''
    def hasYmlChange = false
    def msToPipelineNameMapping = [:]
    def pypiArtifactsToDeployConfig = null
    def artWhl
    def buildInfoWhl
    def cbtClassFileArtifactsUrl = [:]
    def changesetFilesInfo = [:]
    def splitBuildInfo = false
    def pipelineClearedStages = []
    def ignorebuildInfoList = ['repoDetails', 'pipelineName', 'pipelineTag']
    def fullRunCBT = false
    def repoToBaseImageMapping = [:]
    def affectedDownstreamPipelines = []
    def cbtMetrics = [ "ymlTemplateModifiedCount":0, "testcaseAffectedByTemplate":0,
                       "keywordAffectedByTemplate":0, "totalAffectedTestcases":0, 
                       "cbtATCount":0,"tagSpecificCount":0, "affectedPyKwsCount":0 ]
    def platformRepoChanged = false
    def platformRepoName = 'None'
    def hgUrl = ['Bangalore':'mercurialserver.ext.pullme-2.buildme.fnbba.dyn.nesc.nokia.net:2222',
                 'Chennai':'hg-chennai.int.net.nokia.com:2222',
                 'Finland':'mercurialserver.ext.stable-3.buildme.fnbba.dyn.nesc.nokia.net:2222',
                 'built-in':'mercurialserver.ext.stable-3.buildme.fnbba.dyn.nesc.nokia.net:2222']
    def hgMasterUrl = 'hg.be.alcatel-lucent.com'
    def cloudSpecificHgUrl = 'hg.be.alcatel-lucent.com'
    def isDRpipeline = false
    def cbtJavaThresholdLimit = 1300
    def cbtAtThresholdLimit = 1300
}
