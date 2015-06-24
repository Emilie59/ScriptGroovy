import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import hudson.plugins.parameterizedtrigger.*;
import hudson.triggers.*
 
Predicate<AbstractProject> featureProjectPredicate = new Predicate<AbstractProject>() {
  @Override
  public boolean apply(AbstractProject abstractProject) {
    return abstractProject.name ==~ /^Feature_.*/;
  }
}

majorBranches = ["*/master", "*/hotfix/*", "*/tma/*"]
print "Branches = "
majorBranches.each{print(it + " ")}

TriggerDescriptor BITBUCKET_TRIGGER_DESCRIPTOR = Hudson.instance.getDescriptorOrDie(com.cloudbees.jenkins.plugins.BitBucketTrigger.class)

println ""
println "--- [START]"
for(job in Collections2.filter(Jenkins.instance.items, featureProjectPredicate)) {
  println "======="+job.name+"======="
  println "Branches presentes au depart : " + job.scm.getBranches()
  
  // Suppression des branches non présentes par défaut
  Iterator ite = job.scm.getBranches().iterator()
  while (ite.hasNext()) {
    String b = ite.next()
   	 if (!(b in majorBranches)) {
      ite.remove()
    } 
  }
  
  // Ajout des branches par défaut
  branches = job.scm.getBranches().getAt('name')
  majorBranches.each { b-> 
    if(!(branches.contains(b)) ) {
      println("add " + b)
      newB = new hudson.plugins.git.BranchSpec(b)
      job.scm.getBranches().add(newB)
    }  
  }
  
  println "Branches presentes au final : " + job.scm.getBranches()
  
  // Vérifie check "Build when a change is pushed to BitBucket"
  trigger = job.getTriggers().get(BITBUCKET_TRIGGER_DESCRIPTOR)
  
  if (trigger == null || !trigger.getDescriptor().isApplicable(job)) {
    println "Activation trigger BitBucket"
    job.addTrigger(new com.cloudbees.jenkins.plugins.BitBucketTrigger())
  } 
}
println "--- [END]"