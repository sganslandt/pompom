module.exports = function(grunt) {

  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json')
  });

  grunt.registerTask('default', 'Log some stuff.', function() {
    grunt.log.write('Logging').ok();
  });

};