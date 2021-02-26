properties([
    parameters([
        choice(choices: ['Plan', 'Apply', 'Destroy'], description: '', name: 'ACTION'),
        choice(choices: ['Dev', 'QA', 'Prod'], description: '', name: 'ENVIRONMENT'),
        string(defaultValue: '', description: '', name: 'AMI_NAME', trim: true)
        ])
    ])
node("terraform"){
    def ec2_env='dev'
    def ec2_region='us-east-1'
    if(params.ENVIRONMENT == 'QA'){
        ec2_env='qa'
        ec2_region='us-east-2'
    }
    else if (params.ENVIRONMENT == 'Prod'){
        ec2_env='prod'
        ec2_region='us-west-2'
    }
    def tfvar = """
        s3_bucket = "jenkins-terraform-bucker-s3-001"
        s3_folder_project = "terraform_ec2"
        s3_folder_region = "us-east-1"
        s3_folder_type = "class"
        s3_tfstate_file = "infrastructure.tfstate"

        environment = "${ec2_env}"
        region      = "${ec2_region}"
        public_key  = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCZwRr7o/snUnsOljhWEMIVYKAD60QkDjPI1JPOMD/lBS3bttiw/7NxQt5GZHZVkzSw+k3G6s+EwK20EhkAHZ5p9znQVjpfchxjZBcU895MqiquB/Is+fgjUWUqFYtbIDFT05gLGmA+vcTSnI6tgYyundquDPoOp/7nKc4Yum2En6XUEhNmkiQBKTMwh0SecrblCpfcqsO63XCZURZ8j/sVJF7IdSuiSe0SpUeW955JDS7PmQcO4GrmFMHY196oOfGjXXdntjT2boXcf+GDPm9rShwvqHi5D+azLuUPaUbSzJU7KntydOW+pmNAaAoM1kB92m4RdfpD1qSY0C3CDdu3 root@docker"
        ami_name    = "${params.AMI_NAME}"
    """
    stage("Pull Repo"){
        git url: 'https://github.com/ikambarov/terraform-ec2-by-ami-name.git'
    }
    withCredentials([usernamePassword(credentialsId: 'jenkins_aws_keys', passwordVariable: 'AWS_SECRET_ACCESS_KEY', usernameVariable: 'AWS_ACCESS_KEY_ID')]) {
        withEnv(["AWS_REGION=${ec2_region}"]) {
            stage("Terrraform Init"){
                writeFile file: "${ec2_env}.tfvars", text: "${tfvar}"
                sh """
                    source setenv.sh ${ec2_env}.tfvars
                    terraform init
                """
            }        
            if(params.ACTION == 'Destroy'){
                stage("Terraform Destroy"){
                    sh """
                        terraform destroy -var-file ${ec2_env}.tfvars -auto-approve
                    """
                }
            }
            else if(params.ACTION == 'Apply'){
                stage("Terraform Apply"){
                    sh """
                        terraform apply -var-file ${ec2_env}.tfvars -auto-approve
                    """
                }
            }
            else if(params.ACTION == 'Plan') {
                stage("Terraform Plan"){
                    sh """
                        terraform plan -var-file ${ec2_env}.tfvars
                    """
                }
            }       
        }
    }    
}





