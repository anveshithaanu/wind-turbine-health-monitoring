package com.wind.turbinemonitor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Handle static resources (JS, CSS, images, etc.)
        // Check both static/ and static/browser/ directories
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/", "classpath:/static/browser/")
                .resourceChain(true)
                .addResolver(new AngularResourceResolver());
    }
    
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // This ensures root path serves index.html
        // But we handle this in the resolver instead
    }
    
    private static class AngularResourceResolver implements ResourceResolver {
        @Override
        public Resource resolveResource(HttpServletRequest request, String requestPath,
                List<? extends Resource> locations, ResourceResolverChain chain) {
            // Get the actual request URI
            String uri = request.getRequestURI();
            
            // IMPORTANT: Exclude API paths - return null so controllers handle them
            if (uri != null && uri.startsWith("/api/")) {
                return null;
            }
            
            // Also check requestPath for API
            if (requestPath != null && requestPath.startsWith("/api/")) {
                return null;
            }
            
            // Handle favicon explicitly
            if (uri != null && uri.equals("/favicon.ico")) {
                Resource favicon = new ClassPathResource("/static/favicon.ico");
                if (favicon.exists()) {
                    return favicon;
                }
                Resource browserFavicon = new ClassPathResource("/static/browser/favicon.ico");
                if (browserFavicon.exists()) {
                    return browserFavicon;
                }
            }
            
            // Try to resolve the actual resource (JS, CSS, images, etc.)
            Resource resolved = chain.resolveResource(request, requestPath, locations);
            
            // If resource exists, return it
            if (resolved != null && resolved.exists()) {
                return resolved;
            }
            
            // For any other path (including root), return index.html for Angular routing
            // This handles SPA routing - all non-API, non-resource requests get index.html
            try {
                // Try static/index.html first
                Resource indexResource = new ClassPathResource("/static/index.html");
                if (indexResource.exists()) {
                    return indexResource;
                }
                // Fallback to static/browser/index.html
                Resource browserIndex = new ClassPathResource("/static/browser/index.html");
                if (browserIndex.exists()) {
                    return browserIndex;
                }
            } catch (Exception e) {
                // If we can't find index.html, return null to let Spring handle it
            }
            
            return null;
        }
        
        @Override
        public String resolveUrlPath(String resourcePath, List<? extends Resource> locations,
                ResourceResolverChain chain) {
            // Exclude API paths from URL resolution
            if (resourcePath != null && resourcePath.startsWith("/api/")) {
                return null;
            }
            return chain.resolveUrlPath(resourcePath, locations);
        }
    }
}
