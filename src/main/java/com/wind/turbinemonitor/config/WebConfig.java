package com.wind.turbinemonitor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(false)
                .addResolver(new AngularResourceResolver());
    }
    
        private static class AngularResourceResolver implements ResourceResolver {
        @Override
        public Resource resolveResource(HttpServletRequest request, String requestPath,
                List<? extends Resource> locations, ResourceResolverChain chain) {
            if (requestPath != null && requestPath.startsWith("/api/")) {
                return null;
            }
            
            if (requestPath != null && requestPath.equals("/favicon.ico")) {
                Resource favicon = new ClassPathResource("/static/favicon.ico");
                if (favicon.exists()) {
                    return favicon;
                }
            }
            
            Resource resolved = chain.resolveResource(request, requestPath, locations);
            if (resolved == null) {
                try {
                    Resource indexResource = new ClassPathResource("/static/index.html");
                    if (indexResource.exists()) {
                        return indexResource;
                    }
                    Resource browserIndex = new ClassPathResource("/static/browser/index.html");
                    if (browserIndex.exists()) {
                        return browserIndex;
                    }
                } catch (Exception e) {
                }
            }
            return resolved;
        }
        
        @Override
        public String resolveUrlPath(String resourcePath, List<? extends Resource> locations,
                ResourceResolverChain chain) {
            return chain.resolveUrlPath(resourcePath, locations);
        }
    }
}

