def call(String filesToTransfer) {
    def props = variables()
    echo "Files to transfer: ${filesToTransfer}"
    
    sshagent(credentials: ['ec2-ssh-key']) {
        sh """
            # Create a tarball of the files in the workspace root
            tar -czf project_files.tar.gz -C ${props.APP_PATH} ${filesToTransfer}
            
            # Ensure target directory exists on remote host
            ssh -o StrictHostKeyChecking=no ${props.EC2_USER}@${props.EC2_HOST} 'mkdir -p ${props.TARGET_DIR}'
            
            # Transfer the tarball from the workspace root
            scp -o StrictHostKeyChecking=no project_files.tar.gz ${props.EC2_USER}@${props.EC2_HOST}:${props.TARGET_DIR}/
            
            # Untar the files on the remote host and cleanup
            ssh -o StrictHostKeyChecking=no ${props.EC2_USER}@${props.EC2_HOST} 'tar -xzf ${props.TARGET_DIR}/project_files.tar.gz -C ${props.TARGET_DIR} && rm ${props.TARGET_DIR}/project_files.tar.gz'
            
            # Local cleanup
            rm project_files.tar.gz
        """
    }
}
