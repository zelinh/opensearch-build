import jenkins.BuildManifest
import java.io.File

import static org.hamcrest.CoreMatchers.notNullValue
import static org.hamcrest.MatcherAssert.assertThat
//import static org.hamcrest.io.FileMatchers.anExistingFile

class ValidateMetaRPMLibTester extends LibFunctionTester {

    private String distManifest
    private String rpmDistribution

    public ValidateMetaRPMLibTester(distManifest, rpmDistribution){
        this.distManifest = distManifest
        this.rpmDistribution = rpmDistribution
    }

    void parameterInvariantsAssertions(call){
        assertThat(call.args.distManifest.first(), notNullValue())
        assertThat(call.args.rpmDistribution.first(), notNullValue())
        //assertThat(new File(call.args.rpmDistribution.first()),anExistingFile())
    }

    boolean expectedParametersMatcher(call) {
        return call.args.distManifest.first().toString().equals(this.distManifest)
                && call.args.rpmDistribution.first().toString().equals(this.rpmDistribution)
    }

    String libFunctionName(){
        return 'validateMetaRPM'
    }

    void configure(helper, binding){
        binding.setVariable('GITHUB_BOT_TOKEN_NAME', 'dummy_token_name')
        binding.setVariable('GITHUB_USER', 'dummy_user')
        binding.setVariable('GITHUB_TOKEN', 'dummy_token')


    }
}

